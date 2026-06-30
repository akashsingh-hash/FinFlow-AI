import React, { Suspense } from 'react';
import { useNavigate } from 'react-router-dom';

const Spline = React.lazy(() => import('@splinetool/react-spline'));

export default function SentinelHero() {
  const navigate = useNavigate();

  return (
    <section className="relative min-h-screen flex items-end bg-hero-bg overflow-hidden">
      {/* Spline 3D Background canvas */}
      <div className="absolute inset-0">
        <Suspense fallback={<div className="absolute inset-0 bg-hero-bg" />}>
          <Spline
            scene="https://prod.spline.design/Slk6b8kz3LRlKiyk/scene.splinecode"
            className="w-full h-full"
          />
        </Suspense>
      </div>

      {/* Dark tint overlay */}
      <div className="absolute inset-0 bg-black/30 z-[1] pointer-events-none" />

      {/* Content Area - Pointer clicks are passed through to Spline scene except on CTA buttons */}
      <div className="relative z-10 pointer-events-none w-full max-w-[90%] sm:max-w-md lg:max-w-2xl px-6 md:px-10 pb-10 md:pb-10 pt-32">
        {/* Heading */}
        <h1
          className="text-[clamp(3rem,8vw,6rem)] font-bold leading-[1.05] tracking-[-0.05em] text-foreground mb-2 md:mb-4 uppercase opacity-0 animate-fade-up"
          style={{ animationDelay: "0.2s" }}
        >
          FINFLOW<span className="text-primary"> AI</span>
        </h1>

        {/* Subheading */}
        <p
          className="text-foreground/80 text-[clamp(1.125rem,2.5vw,1.875rem)] font-light mb-3 md:mb-6 opacity-0 animate-fade-up font-sora"
          style={{ animationDelay: "0.4s" }}
        >
          We implement financial intelligence correctly.
        </p>

        {/* Description */}
        <p
          className="text-muted-foreground text-[clamp(0.875rem,1.5vw,1.25rem)] font-light mb-4 md:mb-8 opacity-0 animate-fade-up font-sora"
          style={{ animationDelay: "0.55s" }}
        >
          Enterprise finance systems built in days. AI-powered cashflow tracking deployed with zero-trust architecture. Smart expense controls set up for your entire organization. All of it done right, not just fast.
        </p>

        {/* Two CTA Buttons */}
        <div
          className="flex flex-wrap gap-3 font-bold opacity-0 animate-fade-up"
          style={{ animationDelay: "0.7s" }}
        >
          <button
            onClick={() => navigate('/register')}
            className="bg-primary text-primary-foreground px-6 py-3 md:px-8 md:py-4 text-sm rounded-sm cursor-pointer hover:brightness-110 transition-all active:scale-[0.97] pointer-events-auto font-sora font-semibold"
          >
            Book a Call
          </button>
          <button
            onClick={() => navigate('/login')}
            className="bg-white text-background px-6 py-3 md:px-8 md:py-4 text-sm rounded-sm cursor-pointer hover:brightness-90 transition-all active:scale-[0.97] pointer-events-auto font-sora font-semibold"
          >
            Our Work
          </button>
        </div>

        {/* Trust Line */}
        <p
          className="text-muted-foreground/60 text-xs font-light mt-4 md:mt-6 opacity-0 animate-fade-up font-sora"
          style={{ animationDelay: "0.85s" }}
        >
          Trusted financial partner. Columbus, OH. 12 systems deployed.
        </p>
      </div>
    </section>
  );
}
