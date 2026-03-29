import React, { useContext } from 'react';
import './ExploreVenues.css';
import { StoreContext } from '../../context/storeContextInstance';
import Bookvenue from '../Bookvenue/Bookvenue';
import { assets, scroll } from '../../assets/assets';
import { Link } from 'react-router-dom';

const ExploreVenues = () => {
  const { COURT_list, setMenu } = useContext(StoreContext);

  const scrollLeftVenues = () => scroll('explore-venues-list', -300);
  const scrollRightVenues = () => scroll('explore-venues-list', 300);

  if (!COURT_list || COURT_list.length === 0) {
    return <div>No venues available for this category.</div>;
  }

  return (
    <div className='explore-venues-card'>
      <div className="explore-venues">
        <Link to='./book' onClick={() => setMenu('book')}><h1>Explore Venues</h1></Link>
        <div className="explore-venues-container">
          <img className="scroll-icon left-icon" src={assets.leftIcon} alt="Scroll Left" onClick={scrollLeftVenues} />
          <div className="explore-venues-list" id="explore-venues-list">
            {COURT_list.slice(0, 10).map((item) => (
              <Bookvenue
                key={item._id}
                className='explore-venues-list-item'
                courtName={item.courtName}
                courtLocation={item.courtLocation}
                courtsAvailable={item.courtsAvailable}
                price={item.price}
                courtImage={item.courtImage}
                game_icon={item.game_icon}
                sport={item.sport}
                id={item._id}
              />
            ))}
          </div>
          <img className="scroll-icon right-icon" src={assets.rightIcon} alt="Scroll Right" onClick={scrollRightVenues} />
        </div>
      </div>
    </div>
  );
};

export default ExploreVenues;
