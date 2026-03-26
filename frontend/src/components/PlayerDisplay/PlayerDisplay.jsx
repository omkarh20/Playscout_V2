import React, { useContext } from 'react'
import './PlayerDisplay.css'
import { StoreContext } from '../../context/storeContextInstance'
import PlayerObject from '../PlayerObject/PlayerObject'
import { format, isValid, parse } from 'date-fns'

const normalizeGameDate = (gameDateValue) => {
  if (!gameDateValue) return null;

  const dateOnlyPart = gameDateValue
    .replace(/\s+\d{1,2}:\d{2}\s*-\s*\d{1,2}:\d{2}.*$/, '')
    .replace(/\b(\d{1,2})(st|nd|rd|th)\b/gi, '$1')
    .trim();

  const parsedFromExpectedFormat = parse(dateOnlyPart, 'dd MMM, yyyy', new Date());
  if (isValid(parsedFromExpectedFormat)) {
    return format(parsedFromExpectedFormat, 'yyyy-MM-dd');
  }

  const parsedFromNativeDate = new Date(dateOnlyPart);
  if (Number.isNaN(parsedFromNativeDate.getTime())) {
    return null;
  }

  return format(parsedFromNativeDate, 'yyyy-MM-dd');
};

const PlayerDisplay = ({ selectedMeetSport, selectedMeetLocation, startDate}) => {

    const {player_list} = useContext(StoreContext);

    const filteredPlayers = player_list.filter((item)=>{
      const sportMatch = selectedMeetSport === 'Select Sport' || selectedMeetSport === 'All' || item.sportName === selectedMeetSport;
      const locationMatch = selectedMeetLocation === 'Select Location' || selectedMeetLocation === 'All' || item.location === selectedMeetLocation;
      const selectedDateValue = startDate ? format(startDate, 'yyyy-MM-dd') : null;
      const gameDateValue = normalizeGameDate(item.date);
      const dateMatch = selectedDateValue === null || selectedDateValue === gameDateValue;
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