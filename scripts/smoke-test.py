import json
import os
from datetime import datetime, timezone
from decimal import Decimal, ROUND_HALF_UP, localcontext
from urllib.error import HTTPError
from urllib.request import urlopen


base_url = f"http://127.0.0.1:{int(os.environ.get('API_PORT', '8088'))}"
checks = []


def request(path):
    try:
        response = urlopen(base_url + path, timeout=15)
    except HTTPError as error:
        response = error
    with response:
        body = json.loads(response.read(), parse_float=Decimal)
        return response.status, body


def check(name, condition):
    if not condition:
        raise SystemExit(f"FAIL: {name}")
    checks.append(name)


for probe in ("liveness", "readiness"):
    status, body = request(f"/actuator/health/{probe}")
    check(f"{probe} returns UP without internal details", status == 200 and body == {"status": "UP"})

for endpoint in ("env", "metrics"):
    status, _ = request(f"/actuator/{endpoint}")
    check(f"{endpoint} is not exposed", status == 404)

status, currencies = request("/api/currencies")
check("currency list includes CAD, USD, EUR and JPY", status == 200 and
      {"CAD", "USD", "EUR", "JPY"}.issubset(currencies))

status, rates = request("/api/rates?base=CAD")
check("rates use the requested base", status == 200 and rates["base"] == "CAD" and
      rates["rates"]["CAD"]["rate"] == 1)

status, conversion = request("/api/convert?from=cad&to=eur&amount=100")
with localcontext() as context:
    context.prec = 34
    expected = (Decimal(100) * rates["rates"]["EUR"]["rate"]).quantize(
        Decimal("0.01"), rounding=ROUND_HALF_UP)
check("conversion matches the rate table", status == 200 and
      conversion["from"] == "CAD" and conversion["to"] == "EUR" and
      conversion["convertedAmount"] == expected)
check("requests reuse the same cache snapshot", conversion["fetchedAt"] == rates["fetchedAt"])
check("rate publication dates are preserved", conversion["fromRateDate"] ==
      rates["rates"]["EUR"]["baseRateDate"] and conversion["toRateDate"] ==
      rates["rates"]["EUR"]["quoteRateDate"])

status, zero = request("/api/convert?from=CAD&to=EUR&amount=0")
check("zero amount is accepted", status == 200 and zero["convertedAmount"] == 0)

status, yen = request("/api/convert?from=CAD&to=JPY&amount=100")
check("JPY conversion rounds to a whole unit", status == 200 and
      yen["convertedAmount"] == int(yen["convertedAmount"]))

for name, query in (
    ("negative amount", "from=CAD&to=EUR&amount=-1"),
    ("unsupported currency", "from=ZZZ&to=EUR&amount=1"),
    ("missing amount", "from=CAD&to=EUR"),
):
    status, error = request("/api/convert?" + query)
    check(f"{name} returns 400", status == 400 and error["status"] == 400)

print(json.dumps({
    "checked_at": datetime.now(timezone.utc).isoformat(),
    "base_url": base_url,
    "passed": len(checks),
    "currencies_returned": len(currencies),
    "snapshot_fetched_at": rates["fetchedAt"],
    "checks": checks,
}, indent=2))
