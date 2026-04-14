import React, { useState, useEffect } from 'react';
import './Header.css';

const slides = [
  { left: '/volleyball1.jpg',  right: '/volleyball_court1.jpg',  sport: 'Volleyball' },
  { left: '/football1.jpg',   right: '/football_court1.jpg',   sport: 'Football'   },
  { left: '/badminton1.jpg',  right: '/badminton_court1.jpeg', sport: 'Badminton'  },
  { left: '/cricket1.jpg',    right: '/cricket_court1.jpg',    sport: 'Cricket'    },
  { left: '/basketball1.jpg', right: '/basketball_court1.jpg', sport: 'Basketball' },
];

const Header = () => {
  const [current, setCurrent] = useState(0);
  const [animKey, setAnimKey] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrent(prev => (prev + 1) % slides.length);
      setAnimKey(prev => prev + 1);
    }, 4500);
    return () => clearInterval(interval);
  }, []);

  const goTo = (i) => {
    setCurrent(i);
    setAnimKey(prev => prev + 1);
  };

  return (
    <div className="header-card">
      <div className="header">

        {/* ── LEFT PANEL ── */}
        <div className="panel panel-left">
          {slides.map((slide, i) => (
            <div
              key={i}
              className={`slide-bg ${i === current ? 'active' : ''}`}
              style={{ backgroundImage: `url(${slide.left})` }}
            />
          ))}
          <div className="left-overlay" />

          <div className="header-text" key={animKey}>
            <span className="sport-tag">{slides[current].sport}</span>
            <h1>Discover and Book the Best Sports Facilities Near You</h1>
            <h3>Connect with local players and join teams effortlessly</h3>
            <p>
              Whether you're looking for a quick game or planning a tournament,
              PlayScout helps you find available facilities and like-minded
              players, all at your fingertips.
            </p>
            <div className="dots">
              {slides.map((_, i) => (
                <button
                  key={i}
                  className={`dot ${i === current ? 'dot-active' : ''}`}
                  onClick={() => goTo(i)}
                  aria-label={`Go to slide ${i + 1}`}
                />
              ))}
            </div>
          </div>
        </div>

        {/* ── RIGHT PANEL ── */}
        <div className="panel panel-right">
          {slides.map((slide, i) => (
            <div
              key={i}
              className={`slide-bg ${i === current ? 'active' : ''}`}
              style={{ backgroundImage: `url(${slide.right})` }}
            />
          ))}
          <div className="right-overlay" />
        </div>

      </div>
    </div>
  );
};

export default Header;