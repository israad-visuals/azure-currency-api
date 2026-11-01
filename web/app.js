const fq = "https://api.dhawr.com",
  Sr = "https://github.com/israad-visuals/azure-currency-api";
function mq() {
  const e = C.useRef(null);
  return (
    C.useEffect(() => {
      const t = e.current,
        r = t.getContext("2d");
      let i,
        s = [],
        l = 0,
        c = 0;
      const d = () => {
          ((t.width = window.innerWidth), (t.height = window.innerHeight));
        },
        f = () => {
          s = [];
          const S = Math.floor((t.width * t.height) / 18e3);
          for (let R = 0; R < S; R++)
            s.push({
              x: Math.random() * t.width,
              y: Math.random() * t.height,
              vx: (Math.random() - 0.5) * 0.3,
              vy: (Math.random() - 0.5) * 0.3,
              size: Math.random() * 2 + 1,
              opacity: Math.random() * 0.6 + 0.2,
              hue: Math.random() > 0.7 ? 170 : 150,
            });
        },
        m = (S) => {
          (r.beginPath(),
            r.arc(S.x, S.y, S.size, 0, Math.PI * 2),
            (r.fillStyle = `hsla(${S.hue}, 80%, 60%, ${S.opacity})`),
            r.fill(),
            r.beginPath(),
            r.arc(S.x, S.y, S.size * 2.5, 0, Math.PI * 2));
          const R = r.createRadialGradient(S.x, S.y, 0, S.x, S.y, S.size * 2.5);
          (R.addColorStop(0, `hsla(${S.hue}, 80%, 60%, ${S.opacity * 0.4})`),
            R.addColorStop(1, "transparent"),
            (r.fillStyle = R),
            r.fill());
        },
        h = () => {
          for (let S = 0; S < s.length; S++)
            for (let R = S + 1; R < s.length; R++) {
              const T = s[S].x - s[R].x,
                x = s[S].y - s[R].y,
                _ = Math.sqrt(T * T + x * x);
              _ < 100 &&
                (r.beginPath(),
                r.moveTo(s[S].x, s[S].y),
                r.lineTo(s[R].x, s[R].y),
                (r.strokeStyle = `rgba(52, 211, 153, ${0.2 * (1 - _ / 100)})`),
                (r.lineWidth = 0.5),
                r.stroke());
            }
        },
        b = () => {
          (r.clearRect(0, 0, t.width, t.height),
            s.forEach((S) => {
              const R = l - S.x,
                T = c - S.y;
              (Math.sqrt(R * R + T * T) < 120 &&
                ((S.vx -= R * 3e-5), (S.vy -= T * 3e-5)),
                (S.x += S.vx),
                (S.y += S.vy),
                S.x < 0 && (S.x = t.width),
                S.x > t.width && (S.x = 0),
                S.y < 0 && (S.y = t.height),
                S.y > t.height && (S.y = 0),
                (S.vx *= 0.995),
                (S.vy *= 0.995),
                m(S));
            }),
            h(),
            (i = requestAnimationFrame(b)));
        },
        y = (S) => {
          ((l = S.clientX), (c = S.clientY));
        };
      return (
        d(),
        f(),
        b(),
        window.addEventListener("resize", () => {
          (d(), f());
        }),
        window.addEventListener("mousemove", y),
        () => {
          (cancelAnimationFrame(i),
            window.removeEventListener("resize", d),
            window.removeEventListener("mousemove", y));
        }
      );
    }, []),
    v.jsx("canvas", {
      ref: e,
      className: "fixed inset-0 pointer-events-none z-0",
      style: { opacity: 0.6 },
    })
  );
}
function gq() {
  const e = C.useMemo(
    () => [
      { size: 500, x: "-5%", y: "5%", color: "emerald", duration: 25 },
      { size: 350, x: "75%", y: "55%", color: "teal", duration: 30 },
      { size: 400, x: "45%", y: "-5%", color: "emerald", duration: 22 },
      { size: 300, x: "85%", y: "15%", color: "cyan", duration: 28 },
    ],
    [],
  );
  return v.jsx("div", {
    className: "fixed inset-0 overflow-hidden pointer-events-none z-0",
    children: e.map((t, r) =>
      v.jsx(
        me.div,
        {
          className: "absolute rounded-full",
          style: {
            width: t.size,
            height: t.size,
            left: t.x,
            top: t.y,
            background:
              t.color === "emerald"
                ? "radial-gradient(circle, rgba(52,211,153,0.15) 0%, transparent 70%)"
                : t.color === "teal"
                  ? "radial-gradient(circle, rgba(45,212,191,0.12) 0%, transparent 70%)"
                  : "radial-gradient(circle, rgba(34,211,238,0.1) 0%, transparent 70%)",
            filter: "blur(60px)",
          },
          animate: {
            x: [0, 30, -20, 0],
            y: [0, -25, 20, 0],
            scale: [1, 1.05, 0.95, 1],
          },
          transition: {
            duration: t.duration,
            repeat: 1 / 0,
            ease: "easeInOut",
          },
        },
        r,
      ),
    ),
  });
}
function hq() {
  return v.jsxs("div", {
    className: "fixed inset-0 pointer-events-none z-0 overflow-hidden",
    children: [
      v.jsx("div", {
        className: "absolute inset-0",
        style: {
          backgroundImage: `
            linear-gradient(rgba(52,211,153,0.04) 1px, transparent 1px),
            linear-gradient(90deg, rgba(52,211,153,0.04) 1px, transparent 1px)
          `,
          backgroundSize: "60px 60px",
        },
      }),
      v.jsx(me.div, {
        className:
          "absolute left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-emerald-400/30 to-transparent",
        animate: { top: ["0%", "100%"] },
        transition: { duration: 10, repeat: 1 / 0, ease: "linear" },
      }),
    ],
  });
}
function bq() {
  return v.jsxs("div", {
    className: "fixed inset-0 z-0",
    children: [
      v.jsx("div", {
        className:
          "absolute inset-0 bg-gradient-to-br from-slate-900 via-gray-900 to-slate-950",
      }),
      v.jsx("div", {
        className:
          "absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-emerald-950/40 via-transparent to-transparent",
      }),
      v.jsx("div", {
        className:
          "absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_right,_var(--tw-gradient-stops))] from-teal-950/30 via-transparent to-transparent",
      }),
      v.jsx("div", {
        className:
          "absolute inset-0 bg-[radial-gradient(circle_at_center,transparent_0%,rgba(0,0,0,0.3)_100%)]",
      }),
    ],
  });
}
function Tr({ children: e, className: t = "", delay: r = 0 }) {
  const i = C.useRef(null),
    s = XN(i, { once: !0, margin: "-80px" });
  return v.jsx(me.div, {
    ref: i,
    initial: { opacity: 0, y: 50 },
    animate: s ? { opacity: 1, y: 0 } : {},
    transition: { duration: 0.7, delay: r, ease: [0.25, 0.1, 0.25, 1] },
    className: t,
    children: e,
  });
}
function jh({ children: e, className: t = "", staggerDelay: r = 0.1 }) {
  const i = C.useRef(null),
    s = XN(i, { once: !0, margin: "-50px" });
  return v.jsx(me.div, {
    ref: i,
    initial: "hidden",
    animate: s ? "visible" : "hidden",
    variants: { visible: { transition: { staggerChildren: r } } },
    className: t,
    children: e,
  });
}
function $h({ children: e, className: t = "" }) {
  return v.jsx(me.div, {
    variants: { hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0 } },
    transition: { duration: 0.5, ease: [0.25, 0.1, 0.25, 1] },
    className: t,
    children: e,
  });
}
function ki({ children: e, className: t = "", glow: r = !1 }) {
  return v.jsx(me.div, {
    className: `
        relative overflow-hidden rounded-2xl
        bg-gradient-to-br from-white/[0.07] to-white/[0.02]
        border border-white/10
        backdrop-blur-xl
        ${r ? "shadow-[0_0_40px_rgba(52,211,153,0.12)]" : ""}
        ${t}
      `,
    whileHover: {
      borderColor: "rgba(52, 211, 153, 0.4)",
      transition: { duration: 0.2 },
    },
    children: e,
  });
}
function yq({ text: e, className: t = "", delay: r = 0 }) {
  const [i, s] = C.useState(""),
    [l, c] = C.useState(!0);
  return (
    C.useEffect(() => {
      const d = setTimeout(() => {
        let f = 0;
        const m = setInterval(() => {
          f <= e.length
            ? (s(e.slice(0, f)), f++)
            : (clearInterval(m), setTimeout(() => c(!1), 1500));
        }, 35);
        return () => clearInterval(m);
      }, r);
      return () => clearTimeout(d);
    }, [e, r]),
    v.jsxs("span", {
      className: t,
      children: [
        i,
        l &&
          v.jsx(me.span, {
            animate: { opacity: [1, 0] },
            transition: { duration: 0.5, repeat: 1 / 0 },
            className: "text-emerald-400",
            children: "|",
          }),
      ],
    })
  );
}
function Gh({ text: e }) {
  const [t, r] = C.useState(!1);
  return v.jsx(me.button, {
    onClick: () => {
      (navigator.clipboard.writeText(e), r(!0), setTimeout(() => r(!1), 2e3));
    },
    className:
      "flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/25 transition-all",
    whileHover: { scale: 1.05 },
    whileTap: { scale: 0.95 },
    children: t
      ? v.jsxs(v.Fragment, { children: [v.jsx(fP, { size: 12 }), " Copied!"] })
      : v.jsxs(v.Fragment, { children: [v.jsx(gP, { size: 12 }), " Copy"] }),
  });
}
function vq() {
  const [e, t] = C.useState(!1);
  C.useEffect(() => {
    const i = () => t(window.scrollY > 50);
    return (
      window.addEventListener("scroll", i),
      () => window.removeEventListener("scroll", i)
    );
  }, []);
  const r = () =>
    v.jsxs("div", {
      className:
        "flex flex-col gap-[3px] w-8 h-8 sm:w-10 sm:h-10 justify-center items-center p-1.5 rounded-lg bg-black/30 border border-white/10",
      children: [
        v.jsx("div", {
          className: "w-full h-[5px] sm:h-[6px] rounded-sm",
          style: {
            backgroundColor: "#00ff41",
            boxShadow: "0 0 8px #00ff41, 0 0 12px rgba(0, 255, 65, 0.5)",
          },
        }),
        v.jsx("div", {
          className: "w-full h-[5px] sm:h-[6px] rounded-sm bg-white",
        }),
        v.jsx("div", {
          className: "w-full h-[5px] sm:h-[6px] rounded-sm",
          style: { backgroundColor: "#14b8a6" },
        }),
      ],
    });
  return v.jsx(me.nav, {
    initial: { y: -100, opacity: 0 },
    animate: { y: 0, opacity: 1 },
    transition: { duration: 0.6 },
    className: `fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${e ? "bg-slate-900/80 backdrop-blur-2xl border-b border-white/5" : ""}`,
    children: v.jsxs("div", {
      className:
        "max-w-7xl mx-auto px-4 sm:px-6 h-16 sm:h-20 flex items-center justify-between",
      children: [
        v.jsxs(me.a, {
          href: "#",
          className: "flex items-center gap-2 sm:gap-3",
          whileHover: { scale: 1.02 },
          children: [
            v.jsx(r, {}),
            v.jsxs("div", {
              className: "flex flex-col",
              children: [
                v.jsx("span", {
                  className:
                    "text-base sm:text-lg font-bold text-white font-mono leading-tight",
                  children: "Currency Bridge",
                }),
                v.jsx("span", {
                  className:
                    "text-[9px] sm:text-[10px] text-gray-400 font-mono hidden sm:block",
                  children: "Open Source API",
                }),
              ],
            }),
          ],
        }),
        v.jsx("div", {
          className: "hidden md:flex items-center gap-6 lg:gap-8",
          children: ["Docs", "Endpoints", "Contribute"].map((i) =>
            v.jsx(
              me.a,
              {
                href: `#${i.toLowerCase()}`,
                className:
                  "text-gray-400 hover:text-emerald-400 transition-colors text-sm font-medium",
                whileHover: { y: -2 },
                children: i,
              },
              i,
            ),
          ),
        }),
        v.jsxs(me.a, {
          href: Sr,
          target: "_blank",
          rel: "noopener noreferrer",
          className:
            "flex items-center gap-2 px-3 sm:px-5 py-2 sm:py-2.5 rounded-xl bg-white/5 border border-white/10 text-white font-medium text-sm hover:bg-white/10 transition-all",
          whileHover: { scale: 1.05 },
          whileTap: { scale: 0.95 },
          children: [
            v.jsx(vs, { size: 16 }),
            v.jsx("span", {
              className: "hidden sm:inline",
              children: "Star on GitHub",
            }),
          ],
        }),
      ],
    }),
  });
}
function Eq() {
  const { scrollYProgress: e } = tP(),
    t = aS(e, [0, 0.4], [0, -100]),
    r = aS(e, [0, 0.25], [1, 0]);
  return v.jsx("section", {
    className:
      "relative min-h-screen flex items-center justify-center overflow-hidden pt-16",
    children: v.jsxs(me.div, {
      style: { y: t, opacity: r },
      className:
        "relative z-10 max-w-5xl mx-auto px-4 sm:px-6 py-16 sm:py-24 text-center",
      children: [
        v.jsx(me.div, {
          initial: { opacity: 0, y: 20 },
          animate: { opacity: 1, y: 0 },
          transition: { duration: 0.5 },
          className: "mb-6 sm:mb-8",
          children: v.jsxs(me.a, {
            href: Sr,
            target: "_blank",
            rel: "noopener noreferrer",
            className:
              "inline-flex items-center gap-2 sm:gap-3 px-4 sm:px-5 py-2 sm:py-2.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs sm:text-sm font-mono hover:bg-emerald-500/20 transition-all",
            whileHover: { scale: 1.05 },
            children: [
              v.jsx(RP, { size: 14 }),
              "Open Source • MIT License",
              v.jsx(bP, { size: 12 }),
            ],
          }),
        }),
        v.jsx(me.h1, {
          initial: { opacity: 0 },
          animate: { opacity: 1 },
          transition: { duration: 0.5, delay: 0.2 },
          className:
            "text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold mb-4 sm:mb-6 leading-tight",
          children: v.jsx(yq, {
            text: "Custom Currency Bridge API",
            className:
              "bg-gradient-to-r from-white via-gray-100 to-gray-300 bg-clip-text text-transparent",
            delay: 400,
          }),
        }),
        v.jsxs(me.p, {
          initial: { opacity: 0, y: 20 },
          animate: { opacity: 1, y: 0 },
          transition: { duration: 0.5, delay: 2.5 },
          className:
            "text-base sm:text-lg lg:text-xl text-gray-300 max-w-3xl mx-auto mb-4 leading-relaxed px-4",
          children: [
            "A free, open-source RESTful API for ",
            v.jsx("span", {
              className: "text-emerald-400 font-semibold",
              children: "custom currencies through USD",
            }),
            ". Set a USD valuation for game credits or community units in PostgreSQL, then convert them to supported market currencies with Frankfurter.",
          ],
        }),
        v.jsx(me.p, {
          initial: { opacity: 0, y: 20 },
          animate: { opacity: 1, y: 0 },
          transition: { duration: 0.5, delay: 2.7 },
          className:
            "text-sm sm:text-base text-gray-500 max-w-2xl mx-auto mb-8 sm:mb-10 px-4",
          children:
            "Public read endpoints. Manual valuations have expiry dates; market rates use a one-hour cache.",
        }),
        v.jsxs(me.div, {
          initial: { opacity: 0, y: 20 },
          animate: { opacity: 1, y: 0 },
          transition: { duration: 0.5, delay: 2.9 },
          className:
            "flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4 mb-12 sm:mb-16 px-4",
          children: [
            v.jsxs(me.a, {
              href: "#docs",
              className:
                "w-full sm:w-auto flex items-center justify-center gap-2 px-6 sm:px-8 py-3 sm:py-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 text-white font-bold text-base sm:text-lg shadow-lg shadow-emerald-500/25",
              whileHover: {
                scale: 1.03,
                boxShadow: "0 20px 40px rgba(52,211,153,0.3)",
              },
              whileTap: { scale: 0.97 },
              children: [v.jsx(pP, { size: 20 }), "View Documentation"],
            }),
            v.jsxs(me.a, {
              href: Sr,
              target: "_blank",
              rel: "noopener noreferrer",
              className:
                "w-full sm:w-auto flex items-center justify-center gap-2 px-6 sm:px-8 py-3 sm:py-4 rounded-xl bg-white/5 border border-white/10 text-white font-bold text-base sm:text-lg hover:bg-white/10 transition-all",
              whileHover: { scale: 1.03 },
              whileTap: { scale: 0.97 },
              children: [v.jsx(vs, { size: 20 }), "View Source"],
            }),
          ],
        }),
        v.jsxs(me.div, {
          initial: { opacity: 0 },
          animate: { opacity: 1 },
          transition: { delay: 3.1 },
          className: "px-4",
          children: [
            v.jsx("p", {
              className: "text-xs text-gray-500 font-mono mb-3",
              children: "Quick test:",
            }),
            v.jsxs("div", {
              className:
                "inline-flex items-center gap-2 sm:gap-3 px-3 sm:px-5 py-2 sm:py-3 rounded-xl bg-slate-800/50 border border-white/10 font-mono text-xs sm:text-sm",
              children: [
                v.jsx("span", { className: "text-gray-400", children: "$" }),
                v.jsx("code", {
                  className: "text-emerald-400 break-all",
                  children: "curl https://api.dhawr.com/api/custom-currencies",
                }),
                v.jsx(Gh, { text: "curl https://api.dhawr.com/api/custom-currencies" }),
              ],
            }),
          ],
        }),
      ],
    }),
  });
}
function Sq() {
  const [e, t] = C.useState([]),
    [r, i] = C.useState(!0),
    [s, l] = C.useState(null);
  C.useEffect(() => {
    let disposed = false;
    const controller = new AbortController();
    async function loadRates() {
      try {
        const response = await fetch(`${fq}/api/rates?base=USD`, { signal: controller.signal });
        if (!response.ok) throw new Error("Rates temporarily unavailable");
        const data = await response.json();
        const codes = [...Object.keys(data.rates).filter(code => code.length > 3).slice(0, 1), "EUR", "GBP"];
        const cards = codes.filter(code => data.rates[code]).map(code => ({
          id: code, currencyCode: code, rate: data.rates[code].rate,
          source: data.rates[code].quote.source,
          rateDate: data.rates[code].quote.rateDate,
          validUntil: data.rates[code].quote.validUntil
        }));
        if (!disposed) { t(cards); l(null); }
      } catch (error) {
        if (!disposed && error.name !== "AbortError") {
          l("Live rates are temporarily unavailable. Check API readiness or try again."); t([]);
        }
      } finally { if (!disposed) i(false); }
    }
    loadRates();
    const timer = setInterval(loadRates, 60000);
    return () => { disposed = true; clearInterval(timer); controller.abort(); };
  }, []);
  const c = {
    GAME_CREDITS: { flag: "💱", name: "Fictional game credits" },
    EUR: { flag: "🇪🇺", name: "Euro" },
    GBP: { flag: "🇬🇧", name: "British Pound" },
  };
  return v.jsx("section", {
    className: "relative py-16 sm:py-24",
    children: v.jsxs("div", {
      className: "max-w-7xl mx-auto px-4 sm:px-6",
      children: [
        v.jsxs(Tr, {
          className: "text-center mb-10 sm:mb-12",
          children: [
            v.jsxs("span", {
              className:
                "inline-flex items-center gap-2 text-emerald-400 font-mono text-xs sm:text-sm uppercase tracking-widest mb-3 sm:mb-4",
              children: [v.jsx(QN, { size: 14 }), "Live API Response"],
            }),
            v.jsx("h2", {
              className:
                "text-2xl sm:text-3xl lg:text-4xl font-bold text-white mb-3",
              children: "One USD, Multiple Currencies",
            }),
            v.jsxs("p", {
              className: "text-gray-400 text-sm sm:text-base",
              children: [
                "Data from ",
                v.jsx("code", {
                  className: "text-emerald-400",
                  children: "GET /api/rates?base=USD",
                }),
              ],
            }),
          ],
        }),
        s &&
          v.jsx(Tr, {
            className: "mb-6",
            children: v.jsxs("div", {
              className:
                "max-w-2xl mx-auto p-4 rounded-xl bg-yellow-500/10 border border-yellow-500/30 text-yellow-400 text-sm text-center",
              children: [
                v.jsx("p", { children: s }),
                v.jsx("p", {
                  className: "text-xs mt-2 text-yellow-500/70",
                  children: "No fallback rates are shown. Please try again later.",
                }),
              ],
            }),
          }),
        v.jsx(jh, {
          className:
            "grid sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6 max-w-4xl mx-auto",
          staggerDelay: 0.1,
          children: (r ? [1, 2, 3] : e).map((d, f) => {
            const m = typeof d == "number",
              h = m
                ? {}
                : c[d.currencyCode] || { flag: "💱", name: d.currencyCode };
            return v.jsx(
              $h,
              {
                children: v.jsx(ki, {
                  className: "p-5 sm:p-6",
                  glow: !0,
                  children: m
                    ? v.jsxs("div", {
                        className: "animate-pulse",
                        children: [
                          v.jsx("div", {
                            className: "h-8 bg-white/10 rounded mb-3",
                          }),
                          v.jsx("div", {
                            className: "h-10 bg-white/10 rounded",
                          }),
                        ],
                      })
                    : v.jsxs(v.Fragment, {
                        children: [
                          v.jsxs("div", {
                            className: "flex items-center justify-between mb-4",
                            children: [
                              v.jsxs("div", {
                                className: "flex items-center gap-3",
                                children: [
                                  v.jsx("span", {
                                    className: "text-3xl sm:text-4xl",
                                    children: h.flag,
                                  }),
                                  v.jsxs("div", {
                                    children: [
                                      v.jsx("span", {
                                        className:
                                          "text-white font-bold text-lg sm:text-xl block",
                                        children: d.currencyCode,
                                      }),
                                      v.jsx("span", {
                                        className: "text-xs text-gray-500",
                                        children: h.name,
                                      }),
                                    ],
                                  }),
                                ],
                              }),
                              v.jsx(me.div, {
                                className:
                                  "w-2 h-2 rounded-full bg-emerald-400",
                                animate: {
                                  scale: [1, 1.3, 1],
                                  opacity: [1, 0.5, 1],
                                },
                                transition: { duration: 2, repeat: 1 / 0 },
                              }),
                            ],
                          }),
                          v.jsxs("div", {
                            className:
                              "text-2xl sm:text-3xl font-bold text-emerald-400 font-mono",
                            children: [
                              Number(d.rate).toLocaleString(),
                              v.jsx("span", {
                                className:
                                  "text-sm sm:text-base text-gray-500 font-normal ml-2",
                                children: "per USD",
                              }),
                            ],
                          }),
                          v.jsx("p", {
                            className: "text-xs text-gray-500",
                            children: `${d.source === "MANUAL" ? "Manual valuation" : "Frankfurter"} · ${d.rateDate || "USD reference"}${d.validUntil ? ` · Valid through ${d.validUntil}` : ""}`
                          }),
                        ],
                      }),
                }),
              },
              m ? f : d.id,
            );
          }),
        }),
      ],
    }),
  });
}
function Tq() {
  const e = [
  {
    "method": "GET",
    "path": "/api/custom-currencies",
    "desc": "Read configured custom valuations: units per USD, dates and active status. GAME_CREDITS is fictional demo data. Operators update PostgreSQL; there is no public write endpoint.",
    "example": "curl \"https://api.dhawr.com/api/custom-currencies\"",
    "response": "[\n  {\n    \"code\": \"GAME_CREDITS\",\n    \"name\": \"Fictional game credits\",\n    \"unitsPerUsd\": 500,\n    \"decimalPlaces\": 0,\n    \"rateDate\": \"2026-11-01\",\n    \"validUntil\": \"2026-12-01\",\n    \"updatedAt\": \"2026-11-01T00:00:00Z\",\n    \"active\": true\n  }\n]"
  },
  {
    "method": "GET",
    "path": "/api/convert",
    "desc": "Convert custom or market currencies through USD. At 500 fictional credits per USD, 1,000 credits equal USD 2. Unknown codes return 400; expired manual rates return 503.",
    "params": [
      "amount (non-negative number)",
      "from (currency code)",
      "to (currency code)"
    ],
    "example": "curl \"https://api.dhawr.com/api/convert?amount=1000&from=GAME_CREDITS&to=USD\"",
    "response": "{\n  \"from\": \"GAME_CREDITS\",\n  \"to\": \"USD\",\n  \"amount\": 1000,\n  \"exchangeRate\": 0.002,\n  \"convertedAmount\": 2.0,\n  \"bridgeCurrency\": \"USD\",\n  \"usdAmount\": 2\n}"
  },
  {
    "method": "GET",
    "path": "/api/rates",
    "desc": "Read market and active custom rates for a base currency. GET /api/currencies lists supported codes. Market targets such as EUR combine your manual USD valuation with Frankfurter rates. Examples below are response excerpts, not current market quotes.",
    "params": [
      "base (currency code, default USD)"
    ],
    "example": "curl \"https://api.dhawr.com/api/rates?base=USD\"",
    "response": "{\n  \"base\": \"USD\",\n  \"rates\": {\n    \"GAME_CREDITS\": {\n      \"rate\": 500,\n      \"quote\": {\n        \"source\": \"MANUAL\",\n        \"unitsPerUsd\": 500\n      }\n    }\n  }\n}"
  }
];
  return v.jsx("section", {
    id: "endpoints",
    className: "relative py-16 sm:py-24",
    children: v.jsxs("div", {
      className: "max-w-7xl mx-auto px-4 sm:px-6",
      children: [
        v.jsxs(Tr, {
          className: "text-center mb-10 sm:mb-16",
          children: [
            v.jsxs("span", {
              className:
                "inline-flex items-center gap-2 text-emerald-400 font-mono text-xs sm:text-sm uppercase tracking-widest mb-3 sm:mb-4",
              children: [v.jsx(wP, { size: 14 }), "API Reference"],
            }),
            v.jsx("h2", {
              className:
                "text-2xl sm:text-3xl lg:text-4xl font-bold text-white mb-3",
              children: "Available Endpoints",
            }),
            v.jsx("p", {
              className: "text-gray-400 text-sm sm:text-base max-w-xl mx-auto",
              children:
                "Simple REST API. No authentication required for public endpoints.",
            }),
          ],
        }),
        v.jsx(jh, {
          className: "space-y-4 sm:space-y-6 max-w-4xl mx-auto",
          staggerDelay: 0.15,
          children: e.map((t, r) =>
            v.jsx(
              $h,
              {
                children: v.jsxs(ki, {
                  className: "p-4 sm:p-6 lg:p-8",
                  children: [
                    v.jsxs("div", {
                      className:
                        "flex flex-wrap items-center gap-2 sm:gap-3 mb-4",
                      children: [
                        v.jsx("span", {
                          className:
                            "px-3 py-1.5 rounded-lg bg-emerald-500/20 text-emerald-400 font-mono text-sm font-bold",
                          children: t.method,
                        }),
                        v.jsx("code", {
                          className:
                            "text-white font-mono text-sm sm:text-lg break-all",
                          children: t.path,
                        }),
                      ],
                    }),
                    v.jsx("p", {
                      className: "text-gray-400 mb-4 text-sm sm:text-base",
                      children: t.desc,
                    }),
                    t.params &&
                      v.jsx("div", {
                        className: "flex flex-wrap gap-2 mb-4",
                        children: t.params.map((i, s) =>
                          v.jsx(
                            "span",
                            {
                              className:
                                "px-2 py-1 rounded-md bg-white/5 text-gray-400 text-xs font-mono",
                              children: i,
                            },
                            s,
                          ),
                        ),
                      }),
                    v.jsxs("div", {
                      className: "grid lg:grid-cols-2 gap-4",
                      children: [
                        v.jsxs("div", {
                          children: [
                            v.jsxs("div", {
                              className:
                                "flex justify-between items-center mb-2",
                              children: [
                                v.jsx("span", {
                                  className:
                                    "text-xs text-gray-500 font-mono uppercase",
                                  children: "Request",
                                }),
                                v.jsx(Gh, { text: t.example }),
                              ],
                            }),
                            v.jsx("pre", {
                              className:
                                "bg-slate-800/50 rounded-xl p-3 sm:p-4 overflow-x-auto border border-white/5",
                              children: v.jsx("code", {
                                className:
                                  "text-emerald-400 font-mono text-xs sm:text-sm",
                                children: t.example,
                              }),
                            }),
                          ],
                        }),
                        v.jsxs("div", {
                          children: [
                            v.jsx("span", {
                              className:
                                "text-xs text-gray-500 font-mono uppercase block mb-2",
                              children: "Response excerpt",
                            }),
                            v.jsx("pre", {
                              className:
                                "bg-slate-800/50 rounded-xl p-3 sm:p-4 overflow-x-auto border border-white/5",
                              children: v.jsx("code", {
                                className:
                                  "text-gray-300 font-mono text-xs sm:text-sm",
                                children: t.response,
                              }),
                            }),
                          ],
                        }),
                      ],
                    }),
                  ],
                }),
              },
              r,
            ),
          ),
        }),
      ],
    }),
  });
}
const Yx = {
  "javascript": "async function getJson(path) {\n  const response = await fetch(`https://api.dhawr.com${path}`);\n  if (!response.ok) throw new Error(`API returned ${response.status}`);\n  return response.json();\n}\n\nconst currencies = await getJson('/api/currencies');\nconst result = await getJson(\n  '/api/convert?amount=1000&from=GAME_CREDITS&to=EUR'\n);\nconsole.log(result.convertedAmount, result.to);\nconsole.log(result.usdAmount, result.fromQuote, result.toQuote);",
  "python": "import requests\n\nresponse = requests.get(\n    \"https://api.dhawr.com/api/convert\",\n    params={\"amount\": 1000, \"from\": \"GAME_CREDITS\", \"to\": \"EUR\"},\n    timeout=15,\n)\nresponse.raise_for_status()\nresult = response.json()\nprint(result[\"convertedAmount\"], result[\"to\"])\nprint(result[\"usdAmount\"], result[\"fromQuote\"], result[\"toQuote\"])",
  "curl": "# Inspect available codes and manual valuations\ncurl --fail-with-body \"https://api.dhawr.com/api/currencies\"\ncurl --fail-with-body \"https://api.dhawr.com/api/custom-currencies\"\n\n# 1,000 credits / 500 credits per USD = USD 2\ncurl --fail-with-body \"https://api.dhawr.com/api/convert?amount=1000&from=GAME_CREDITS&to=USD\"\n\n# Use the market side of the USD bridge for EUR\ncurl --fail-with-body \"https://api.dhawr.com/api/convert?amount=1000&from=GAME_CREDITS&to=EUR\""
};
function wq() {
  const [e, t] = C.useState("javascript");
  return v.jsx("section", {
    id: "docs",
    className: "relative py-16 sm:py-24",
    children: v.jsxs("div", {
      className: "max-w-7xl mx-auto px-4 sm:px-6",
      children: [
        v.jsxs(Tr, {
          className: "text-center mb-10 sm:mb-16",
          children: [
            v.jsxs("span", {
              className:
                "inline-flex items-center gap-2 text-emerald-400 font-mono text-xs sm:text-sm uppercase tracking-widest mb-3 sm:mb-4",
              children: [v.jsx(mP, { size: 14 }), "Quick Start"],
            }),
            v.jsx("h2", {
              className:
                "text-2xl sm:text-3xl lg:text-4xl font-bold text-white mb-3",
              children: "Integration Examples",
            }),
            v.jsx("p", {
              className: "text-gray-400 text-sm sm:text-base",
              children: "Copy and paste. Ship in minutes.",
            }),
          ],
        }),
        v.jsx(Tr, {
          delay: 0.2,
          children: v.jsxs(ki, {
            className: "max-w-4xl mx-auto overflow-hidden",
            glow: !0,
            children: [
              v.jsxs("div", {
                className:
                  "flex items-center justify-between px-4 sm:px-6 py-3 sm:py-4 bg-slate-800/50 border-b border-white/10",
                children: [
                  v.jsxs("div", {
                    className: "flex items-center gap-2 sm:gap-3",
                    children: [
                      v.jsxs("div", {
                        className: "flex gap-1.5 sm:gap-2",
                        children: [
                          v.jsx("span", {
                            className:
                              "w-2.5 h-2.5 sm:w-3 sm:h-3 rounded-full bg-red-500",
                          }),
                          v.jsx("span", {
                            className:
                              "w-2.5 h-2.5 sm:w-3 sm:h-3 rounded-full bg-yellow-500",
                          }),
                          v.jsx("span", {
                            className:
                              "w-2.5 h-2.5 sm:w-3 sm:h-3 rounded-full bg-green-500",
                          }),
                        ],
                      }),
                      v.jsx("span", {
                        className: "text-gray-500 font-mono text-xs sm:text-sm",
                        children: "examples.sh",
                      }),
                    ],
                  }),
                  v.jsx(Gh, { text: Yx[e] }),
                ],
              }),
              v.jsx("div", {
                className: "flex border-b border-white/10 overflow-x-auto",
                children: ["javascript", "python", "curl"].map((r) =>
                  v.jsx(
                    "button",
                    {
                      onClick: () => t(r),
                      className: `flex-1 min-w-[100px] px-4 sm:px-6 py-3 font-mono text-xs sm:text-sm transition-all whitespace-nowrap ${e === r ? "text-emerald-400 bg-emerald-500/10 border-b-2 border-emerald-400" : "text-gray-500 hover:text-gray-300"}`,
                      children: r.charAt(0).toUpperCase() + r.slice(1),
                    },
                    r,
                  ),
                ),
              }),
              v.jsx(li, {
                mode: "wait",
                children: v.jsx(
                  me.div,
                  {
                    initial: { opacity: 0 },
                    animate: { opacity: 1 },
                    exit: { opacity: 0 },
                    transition: { duration: 0.2 },
                    children: v.jsx(wk, {
                      language: e === "curl" ? "bash" : e,
                      style: pq,
                      customStyle: {
                        background: "rgba(15, 23, 42, 0.5)",
                        padding: "1.5rem",
                        margin: 0,
                        fontSize: "0.8rem",
                      },
                      showLineNumbers: !0,
                      children: Yx[e],
                    }),
                  },
                  e,
                ),
              }),
            ],
          }),
        }),
      ],
    }),
  });
}
function Rq() {
  const e = [
    {
      icon: NP,
      title: "Custom USD Valuations",
      desc: "Store the number of custom units equal to one US dollar.",
    },
    {
      icon: kh,
      title: "Transparent Rate Sources",
      desc: "Responses include manual and provider sources, dates and the USD bridge amount.",
    },
    {
      icon: tk,
      title: "No Auth Required",
      desc: "Public endpoints. No API keys needed.",
    },
    {
      icon: vP,
      title: "Supported Market Currencies",
      desc: "Use /api/currencies to see the current supported codes.",
    },
    {
      icon: AP,
      title: "Explicit Freshness",
      desc: "Manual rates expire; provider snapshots are cached for one hour.",
    },
    {
      icon: TP,
      title: "MIT Licensed",
      desc: "Use it anywhere, modify anything.",
    },
  ];
  return v.jsx("section", {
    className: "relative py-16 sm:py-24",
    children: v.jsxs("div", {
      className: "max-w-7xl mx-auto px-4 sm:px-6",
      children: [
        v.jsxs(Tr, {
          className: "text-center mb-10 sm:mb-16",
          children: [
            v.jsx("h2", {
              className:
                "text-2xl sm:text-3xl lg:text-4xl font-bold text-white mb-3",
              children: "Why Use This API?",
            }),
            v.jsx("p", {
              className: "text-gray-400 text-sm sm:text-base",
              children:
                "Connect operator-managed units to provider-supported market currencies.",
            }),
          ],
        }),
        v.jsx(jh, {
          className: "grid sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6",
          staggerDelay: 0.1,
          children: e.map((t, r) =>
            v.jsx(
              $h,
              {
                children: v.jsxs(ki, {
                  className: "p-5 sm:p-6 h-full",
                  children: [
                    v.jsx(me.div, {
                      className:
                        "w-10 h-10 sm:w-12 sm:h-12 rounded-xl bg-emerald-500/15 flex items-center justify-center mb-4",
                      whileHover: { scale: 1.1, rotate: 5 },
                      children: v.jsx(t.icon, {
                        size: 22,
                        className: "text-emerald-400",
                      }),
                    }),
                    v.jsx("h3", {
                      className:
                        "text-base sm:text-lg font-bold text-white mb-2",
                      children: t.title,
                    }),
                    v.jsx("p", {
                      className: "text-gray-400 text-sm",
                      children: t.desc,
                    }),
                  ],
                }),
              },
              r,
            ),
          ),
        }),
      ],
    }),
  });
}
function Aq() {
  return v.jsx("section", {
    id: "contribute",
    className: "relative py-16 sm:py-24",
    children: v.jsx("div", {
      className: "max-w-7xl mx-auto px-4 sm:px-6",
      children: v.jsx(Tr, {
        children: v.jsxs(ki, {
          className: "max-w-3xl mx-auto p-6 sm:p-10 text-center",
          glow: !0,
          children: [
            v.jsx(me.div, {
              className:
                "w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center mx-auto mb-6",
              whileHover: { scale: 1.1, rotate: 5 },
              children: v.jsx(yP, { size: 28, className: "text-white" }),
            }),
            v.jsx("h2", {
              className: "text-2xl sm:text-3xl font-bold text-white mb-4",
              children: "Open Source Project",
            }),
            v.jsx("p", {
              className:
                "text-gray-400 mb-6 leading-relaxed text-sm sm:text-base",
              children:
                "Custom Currency Bridge is an open-source learning project. Contributions, bug reports, and feature requests are welcome!",
            }),
            v.jsxs("div", {
              className:
                "flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4",
              children: [
                v.jsxs(me.a, {
                  href: Sr,
                  target: "_blank",
                  rel: "noopener noreferrer",
                  className:
                    "w-full sm:w-auto flex items-center justify-center gap-2 px-5 sm:px-6 py-3 rounded-xl bg-white text-slate-900 font-bold hover:bg-gray-100 transition-all",
                  whileHover: { scale: 1.03 },
                  whileTap: { scale: 0.97 },
                  children: [v.jsx(vs, { size: 18 }), "View on GitHub"],
                }),
                v.jsx(me.a, {
                  href: `${Sr}/issues`,
                  target: "_blank",
                  rel: "noopener noreferrer",
                  className:
                    "w-full sm:w-auto flex items-center justify-center gap-2 px-5 sm:px-6 py-3 rounded-xl bg-white/5 border border-white/10 text-white font-bold hover:bg-white/10 transition-all",
                  whileHover: { scale: 1.03 },
                  whileTap: { scale: 0.97 },
                  children: "Report Issue",
                }),
              ],
            }),
            v.jsx("div", {
              className: "mt-8 pt-6 border-t border-white/10",
              children: v.jsx("p", {
                className: "text-xs text-gray-500 font-mono",
                children:
                  "Tech Stack: Spring Boot • PostgreSQL • React • Tailwind CSS",
              }),
            }),
          ],
        }),
      }),
    }),
  });
}
function xq() {
  return v.jsx("footer", {
    className: "relative py-8 sm:py-12 border-t border-white/5",
    children: v.jsx("div", {
      className: "max-w-7xl mx-auto px-4 sm:px-6",
      children: v.jsxs("div", {
        className:
          "flex flex-col sm:flex-row items-center justify-between gap-4",
        children: [
          v.jsxs("div", {
            className: "flex items-center gap-3",
            children: [
              v.jsx("div", {
                className:
                  "w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center",
                children: v.jsx(ek, { size: 16, className: "text-white" }),
              }),
              v.jsx("span", {
                className: "text-white font-bold",
                children: "Currency Bridge",
              }),
            ],
          }),
          v.jsxs("div", {
            className: "flex items-center gap-4 sm:gap-6",
            children: [
              v.jsx("a", {
                href: Sr,
                target: "_blank",
                rel: "noopener noreferrer",
                className:
                  "text-gray-400 hover:text-emerald-400 transition-colors",
                children: v.jsx(vs, { size: 20 }),
              }),
              v.jsx("a", {
                href: "#docs",
                className:
                  "text-gray-400 hover:text-emerald-400 transition-colors text-sm",
                children: "Docs",
              }),
              v.jsx("a", {
                href: `${Sr}/issues`,
                target: "_blank",
                rel: "noopener noreferrer",
                className:
                  "text-gray-400 hover:text-emerald-400 transition-colors text-sm",
                children: "Issues",
              }),
            ],
          }),
          v.jsxs("p", {
            className: "text-xs sm:text-sm text-gray-500",
            children: [
              "MIT License • Made with ",
              v.jsx(EP, { size: 12, className: "inline text-emerald-400" }),
            ],
          }),
        ],
      }),
    }),
  });
}
function _q() {
  return v.jsxs("div", {
    className: "min-h-screen text-white antialiased overflow-x-hidden",
    children: [
      v.jsx(bq, {}),
      v.jsx(hq, {}),
      v.jsx(gq, {}),
      v.jsx(mq, {}),
      v.jsxs("div", {
        className: "relative z-10",
        children: [
          v.jsx(vq, {}),
          v.jsx(Eq, {}),
          v.jsx(Sq, {}),
          v.jsx(Tq, {}),
          v.jsx(wq, {}),
          v.jsx(Rq, {}),
          v.jsx(Aq, {}),
          v.jsx(xq, {}),
        ],
      }),
    ],
  });
}

function OperatorPage() {
  return v.jsxs("div", { className: "min-h-screen text-white antialiased overflow-x-hidden", children: [
    v.jsx(bq, {}), v.jsx(hq, {}), v.jsx(vq, {}),
    v.jsx("main", { className: "relative z-10 max-w-3xl mx-auto px-4 sm:px-6 py-16 sm:py-24", children:
      v.jsxs(ki, { className: "p-5 sm:p-6", children: [
        v.jsx("h1", { className: "text-2xl sm:text-3xl font-bold text-white mb-3", children: "Manage custom currency valuations" }),
        v.jsx("p", { className: "text-gray-400 text-sm sm:text-base", children: "Custom rates are managed directly in PostgreSQL. The public API supports reading and conversion; the previous browser login and update endpoint are retired." }),
        v.jsx("a", { href: `${Sr}/blob/main/docs/custom-currencies.md`, className: "text-emerald-400", children: "Read the operator SQL guide" }),
        v.jsx("p", { className: "text-xs text-gray-500", children: "Set units per US dollar and validity dates. GAME_CREDITS uses a fictional valuation for demonstration." })
      ] })
    })
  ] });
}
_I.createRoot(document.getElementById("root")).render(
  v.jsx(C.StrictMode, { children: v.jsx(K0, { children: v.jsxs(A0, { children: [
    v.jsx(bg, { path: "/", element: v.jsx(_q, {}) }),
    v.jsx(bg, { path: "/ismail", element: v.jsx(OperatorPage, {}) })
  ] }) }) })
);
