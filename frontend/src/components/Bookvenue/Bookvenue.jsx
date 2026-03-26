import React, { useContext, useState } from 'react';
import './Bookingvenue.css'; 
import Bookpop from '../Bookpop/Bookpop';
import CreateGame from '../CreateGame/CreateGame';
import { StoreContext } from '../../context/storeContextInstance';
import { Link } from 'react-router-dom';

const Bookvenue = ({ id, className, courtName, courtLocation, courtsAvailable, price, courtImage, game_icon, sport }) => {
  const [showBooking, setShowBooking] = useState(false);
  const [showCreateGame, setShowCreateGame] = useState(false);
  const { getImageUrl } = useContext(StoreContext);

  if (!courtName || !courtLocation || !courtImage || courtsAvailable <= 0 || price === undefined) {
    return null; 
  }

  const courtDetails = {
    id,
    courtName,
    courtLocation,
    price,
    game_icon,
    sport,
    courtImage
  };

  const handleBookClick = (event) => {
    event.stopPropagation();
    setShowBooking(true);
  };

  const handleCreateClick = (event) => {
    event.stopPropagation();
    setShowCreateGame(true);
  };

  return (
    <div className={`court-object ${className}`}>
      <div className="court-component">
      <Link key={id} to={`/facility/${id}`} className="court-link">
        <div className="court-header">
          <img 
            src={getImageUrl(courtImage)}
            alt={courtName} 
            className="court-image" 
          />
        </div>

        <div className="court-content">
          <div className="court-info">
            <span className="court-name">{courtName}</span><br/>
            <img src={getImageUrl(game_icon)} className="game_icon" alt={sport} /> 
            <p className="sport-name">{sport}</p>
            <p className="court-location">{courtLocation}</p>
            <p className="court-price">Price: ₹{price}/hr</p>
          </div>
        </div>
       </Link>
        <div className="court-buttons">
          <button className="book-button" onClick={handleBookClick}>Book</button>
          <button className="create-button" onClick={handleCreateClick}>Create</button>
        </div>
      </div>

      {showBooking && <Bookpop setShowBooking={setShowBooking} courtDetails={courtDetails} />}
      {showCreateGame && <CreateGame setShowCreateGame={setShowCreateGame} courtDetails={courtDetails} />}
    </div>
  );
}

export default Bookvenue;
