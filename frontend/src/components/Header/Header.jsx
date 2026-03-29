import React, { useState, useEffect } from 'react';
import './Header.css';

const Header = () => {
  const leftImages = ['/volleyball1.jpg', '/football1.jpg', '/badminton1.jpg', 'cricket1.jpg', 'basketball1.jpg'];
  const rightImages = ['/volleyball_court1.jpg', '/football_court1.jpg', '/badminton_court1.jpeg', 'cricket_court1.jpg', 'basketball_court1.jpg'];

  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentIndex((prevIndex) => (prevIndex + 1) % leftImages.length);
    }, 3000);

    return () => clearInterval(interval);
  }, [leftImages.length]);

  return (
    <div className="header-card">
      <div className='header'>
        <div className="header-contents-left" style={{ backgroundImage: `url(${leftImages[currentIndex]})` }}>
          <h1>Discover and Book the Best Sports Facilities Near You</h1>
          <h3>Connect with local players and join teams effortlessly</h3>
          <p>
            Whether you're looking for a quick game or planning a tournament, PlayScout helps you find available facilities and like-minded players, all at your fingertips.
          </p>
        </div>
        <div className="header-contents-right" style={{ backgroundImage: `url(${rightImages[currentIndex]})` }}></div>
      </div>
    </div>
  );
};

export default Header;
