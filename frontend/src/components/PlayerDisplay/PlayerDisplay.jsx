import React, { useContext } from 'react'
import './PlayerDisplay.css'
import { StoreContext } from '../../context/StoreContext'
import PlayerObject from '../PlayerObject/PlayerObject'

const PlayerDisplay = ({ selectedMeetSport, selectedMeetLocation, startDate}) => {

    const {player_list} = useContext(StoreContext);

    const filteredPlayers = player_list.filter((item)=>{
      const sportMatch = selectedMeetSport === 'Select Sport' || selectedMeetSport === 'All' || item.sportName === selectedMeetSport;
      const locationMatch = selectedMeetLocation === 'Select Location' || selectedMeetLocation === 'All' || item.location === selectedMeetLocation;
      const dateMatch = startDate === null || item.filterDate === startDate.toLocaleDateString();
      return sportMatch && locationMatch && dateMatch;
    });

  return (
    <div className='player-display'>
      <div className="player-display-list">
        {filteredPlayers.map((item,index)=>{
            return <PlayerObject 
                      key={index} 
                      id={item._id} 
                      className = 'player-display-list-item'
                      date={item.date}
                      filterDate={item.filterDate}
                      sportIcon={item.sportIcon} 
                      sportName={item.sportName}
                      userImage={item.userImage} 
                      userName={item.userName}
                      userID={item.userID} 
                      membersJoined={item.membersJoined} 
                      totalMembers={item.totalMembers} 
                      level={item.level}
                      courtName={item.courtName}
                      location={item.location}
                    />
        })}
      </div>
    </div>
  )
}

export default PlayerDisplay