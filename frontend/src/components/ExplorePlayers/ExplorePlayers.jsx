import React, { useContext } from 'react';
import './ExplorePlayers.css';
import { assets, scroll } from '../../assets/assets';
import { StoreContext } from '../../context/storeContextInstance';
import PlayerObject from '../PlayerObject/PlayerObject';
import { Link } from 'react-router-dom';

const ExplorePlayers = () => {
  const { player_list, setMenu } = useContext(StoreContext);

  const scrollLeftPlayers = () => scroll('explore-players-list', -300);
  const scrollRightPlayers = () => scroll('explore-players-list', 300);

  return (
    <div className='explore-players-card'>
      <div className="explore-players">
        <Link to='./meet' onClick={() => setMenu('meet')}><h1>Find Games</h1></Link>
        <div className="explore-players-container">
          <img className="scroll-icon left-icon" src={assets.leftIcon} alt="Scroll Left" onClick={scrollLeftPlayers} />
          <div className="explore-players-list">
            {player_list.slice(0, 10).map((item, index) => {
              return <PlayerObject
                key={index}
                id={item._id}
                date={item.date}
                sportIcon={item.sportIcon}
                sportName={item.sportName}
                userImage={item.userImage}
                userName={item.userName}
                membersJoined={item.membersJoined}
                totalMembers={item.totalMembers}
                courtName={item.courtName}
                level={item.level}
                location={item.location}
              />;
            })}
          </div>
          <img className="scroll-icon right-icon" src={assets.rightIcon} alt="Scroll Right" onClick={scrollRightPlayers} />
        </div>
      </div>
    </div>
  );
};

export default ExplorePlayers;
