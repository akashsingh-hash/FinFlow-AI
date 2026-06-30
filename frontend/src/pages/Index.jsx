import React from 'react';
import SentinelNavbar from '../components/SentinelNavbar';
import SentinelHero from '../components/SentinelHero';

export default function Index() {
  return (
    <div className="bg-hero-bg min-h-screen font-sora selection:bg-primary selection:text-primary-foreground">
      <SentinelNavbar />
      <SentinelHero />
    </div>
  );
}
