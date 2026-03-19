import React, { useContext } from 'react';
import './JoinPopup.css';
import { assets } from '../../assets/assets';
import axios from 'axios';
import { StoreContext } from '../../context/StoreContext';
import { toast } from 'react-toastify';

const JoinPopup = ({ setShowJoinPopup, recipientId, gameId }) => {
  const { url } = useContext(StoreContext);
  const token = localStorage.getItem('token');

  const handleConfirmJoin = async () => {
    try {
      const response = await axios.post(
        `${url}/api/join/send-request`, 
        { recipientId, gameId}, 
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.data.success) {
        toast.success(response.data.message);
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      console.error("Error sending join request:", error);
      toast.error("Failed to send join request.");
    }

    setShowJoinPopup(false);
  };

  return (
    <div className='join-popup'>
      <div className="join-popup-container">
        <div className="join-popup-header">
          <h2>Join Request</h2>
          <img 
            onClick={() => setShowJoinPopup(false)} 
            src={assets.cross_icon} 
            alt="Close" 
            className='join-popup-close'
          />
        </div>
        <div className="join-popup-content">
          <p>Do you want to send a join request to this player?</p>
          <div className="join-popup-actions">
            <button className="confirm-button" onClick={handleConfirmJoin}>Yes</button>
            <button className="cancel-button" onClick={() => setShowJoinPopup(false)}>No</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default JoinPopup;
