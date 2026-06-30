import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button } from './ui/Button';

export default function SentinelNavbar() {
  const { user } = useAuth();
  
  const navLinks = [
    { name: "Services", href: "#services" },
    { name: "About Us", href: "#about" },
    { name: "Projects", href: "#projects" },
    { name: "Team", href: "#team" },
    { name: "Contacts", href: "#contacts" }
  ];

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 flex items-center justify-between px-8 lg:px-16 py-5 bg-transparent">
      {/* Left Brand Logo */}
      <Link to="/" className="text-foreground text-xl font-semibold tracking-tight hover:opacity-90 transition-opacity">
        FINFLOW<span className="text-primary"> AI</span>
      </Link>

      {/* Center Navigation Links */}
      <div className="hidden md:flex items-center gap-8">
        {navLinks.map((link) => (
          <a
            key={link.name}
            href={link.href}
            className="text-xs text-muted-foreground hover:text-foreground transition-colors uppercase tracking-widest"
          >
            {link.name}
          </a>
        ))}
      </div>

      {/* Right Get Quote / Open App Button */}
      <div className="flex items-center gap-4">
        <Link to={user ? "/dashboard" : "/login"}>
          <Button
            variant="navCta"
            size="lg"
            className="hidden md:inline-flex rounded-lg uppercase text-xs tracking-widest px-6"
          >
            {user ? "Open App" : "Get Quote"}
          </Button>
        </Link>
      </div>
    </nav>
  );
}
