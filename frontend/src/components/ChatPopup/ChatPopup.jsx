import './ChatPopup.css';
import { assets } from '../../assets/assets';
import { useNavigate } from 'react-router-dom';

const ChatPopup = ({ setShowChatPopup, recipientId, recipientName, userImage }) => {
  const navigate = useNavigate();

  const handleConfirmChat = () => {
    setShowChatPopup(false);
    navigate('/chat', { state: { recipientId, recipientName, userImage } });
  };

  return (
    <div className='chat-popup'>
      <div className="chat-popup-container">
        <div className="chat-popup-header">
          <h2>Start Chat</h2>
          <img
            onClick={() => setShowChatPopup(false)}
            src={assets.cross_icon}
            alt="Close"
            className='chat-popup-close'
          />
        </div>
        <div className="chat-popup-content">
          <p>Do you want to chat with this person?</p>
          <div className="chat-popup-actions">
            <button className="confirm-button" onClick={handleConfirmChat}>Yes</button>
            <button className="cancel-button" onClick={() => setShowChatPopup(false)}>No</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatPopup;