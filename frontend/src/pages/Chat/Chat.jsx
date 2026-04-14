import React, { useState, useContext, useEffect, useRef } from 'react';
import './Chat.css';
import axios from 'axios';
import { StoreContext } from '../../context/storeContextInstance';
import { toast } from 'react-toastify';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { useLocation } from 'react-router-dom';

const Chat = () => {
  const { url, token, role, userId: currentUserId, getImageUrl } = useContext(StoreContext);
  const location = useLocation();
  const { recipientId, recipientName, userImage } = location.state || {};
  const stompClientRef = useRef(null);
  const deniedToastShownRef = useRef(false);

  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const messagesEndRef = useRef(null);
  const canAccessChat = role === 'PLAYER';

  useEffect(() => {
    if (token && !canAccessChat && !deniedToastShownRef.current) {
      toast.error('Access denied');
      deniedToastShownRef.current = true;
    }
  }, [token, canAccessChat]);

  const getAvatarUrl = (imagePath) => {
    const resolvedPath = imagePath?.trim() ? imagePath : 'avatars/m_avatar2.png';
    return getImageUrl(resolvedPath);
  };

  const addMessageIfMissing = (incomingMessage) => {
    setMessages((prevMessages) => {
      if (!incomingMessage?._id) {
        return [...prevMessages, incomingMessage];
      }
      const alreadyExists = prevMessages.some((message) => message._id === incomingMessage._id);
      return alreadyExists ? prevMessages : [...prevMessages, incomingMessage];
    });
  };

  useEffect(() => {
    const fetchInitialData = async () => {
      if (!token || !canAccessChat) return;
      try {
        const res = await axios.get(`${url}/api/messages`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const allMessages = res.data.data;
        setMessages(allMessages);

        const uniqueUsersMap = {};
        allMessages.forEach(msg => {
          const user = msg.senderId._id === currentUserId ? msg.recipientId : msg.senderId;
          if (user && user._id) {
            uniqueUsersMap[user._id] = user;
          }
        });


        const uniqueUsers = Object.values(uniqueUsersMap).sort((a, b) => {
          const lastMsgA = [...allMessages].reverse().find(
            msg => msg.senderId._id === a._id || msg.recipientId._id === a._id
          );
          const lastMsgB = [...allMessages].reverse().find(
            msg => msg.senderId._id === b._id || msg.recipientId._id === b._id
          );
          return new Date(lastMsgB?.timestamp) - new Date(lastMsgA?.timestamp);
        });

        setUsers(uniqueUsers);

        if (recipientId) {
          const existingUser = uniqueUsers.find(u => u._id === recipientId);
          if (existingUser) {
            setSelectedUser(existingUser);
          } else {
            const newUser = {
              _id: recipientId,
              name: recipientName,
              userImage: userImage || 'avatars/m_avatar1.png'
            };
            setUsers(prev => [...prev, newUser]);
            setSelectedUser(newUser);
          }
        } else if (!selectedUser && uniqueUsers.length > 0) {
          setSelectedUser(uniqueUsers[0]);
        }
      } catch (err) {
        if (err?.response?.status === 403) {
          toast.error('Access denied');
        } else {
          toast.error('Failed to load messages');
        }
      }
    };

    fetchInitialData();
  }, [url, token, currentUserId, recipientId, canAccessChat]);

  useEffect(() => {
    if (!token || !currentUserId || !canAccessChat) {
      return undefined;
    }

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(`${url}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      reconnectDelay: 5000,
      debug: () => {}
    });

    stompClient.onConnect = () => {
      stompClient.subscribe(`/queue/messages.${currentUserId}`, (frame) => {
        try {
          const incomingMessage = JSON.parse(frame.body);
          addMessageIfMissing(incomingMessage);
        } catch {
          toast.error('Received invalid message payload');
        }
      });
    };

    stompClient.onWebSocketError = () => {
      toast.error('Chat connection failed');
    };

    stompClient.onStompError = () => {
      toast.error('Chat session error');
    };

    stompClient.activate();
    stompClientRef.current = stompClient;

    return () => {
      stompClient.deactivate();
      stompClientRef.current = null;
    };
  }, [url, token, currentUserId, canAccessChat]);

  const handleSendMessage = async () => {
    if (!canAccessChat) {
      toast.error('Access denied');
      return;
    }

    if (!inputText.trim() || !selectedUser || !token) return;

    const payload = {
      recipientId: selectedUser._id,
      content: inputText.trim()
    };

    try {
      const client = stompClientRef.current;
      if (client?.connected) {
        client.publish({
          destination: '/app/chat.send',
          body: JSON.stringify(payload)
        });
      } else {
        const res = await axios.post(`${url}/api/messages`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (res?.data?.data) {
          addMessageIfMissing(res.data.data);
        }
      }
      setInputText('');
    } catch {
      toast.error('Failed to send message');
    }
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages]);

  return (
    <div className="chat-container">
      <div className="chat-sidebar">
        <div className="chat-sidebar-header">Conversations</div>
        {users.map((user) => (
          <div
            key={user._id}
            className={`chat-user-entry ${selectedUser?._id === user._id ? 'selected' : ''}`}
            onClick={() => setSelectedUser(user)}
          >
            <img
              src={getAvatarUrl(user.userImage)}
              className="chat-user-icon"
              alt="User"
            />
            <div className="chat-user-name">{user.name}</div>
          </div>
        ))}
      </div>

      <div className="chat-main">
        {!canAccessChat ? (
          <div className="no-chat-selected">
            <h2>Access denied</h2>
          </div>
        ) : selectedUser ? (
          <>
            <div className="chat-header">
              <img
                src={getAvatarUrl(selectedUser.userImage)}
                className="chat-user-icon"
                alt="User"
              />
              <div className="chat-header-name">{selectedUser.name}</div>
            </div>

            <div className="chat-messages">
              {messages
                .filter(msg =>
                  (msg.senderId._id === currentUserId && msg.recipientId._id === selectedUser._id) ||
                  (msg.senderId._id === selectedUser._id && msg.recipientId._id === currentUserId)
                )
                .map((msg) => (
                  <div
                    key={msg._id}
                    className={`chat-message ${msg.senderId._id === currentUserId ? 'sent' : 'received'}`}
                  >
                    <div className="message-content">{msg.content}</div>
                    <div className="message-timestamp">
                      {msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </div>
                  </div>
                ))}
              <div ref={messagesEndRef} />
            </div>

            <div className="chat-input-area">
              <input
                type="text"
                value={inputText}
                onChange={e => setInputText(e.target.value)}
                placeholder="Type a message..."
                className="chat-input"
                onKeyDown={e => {
                  if (e.key === 'Enter') handleSendMessage();
                }}
              />
              <button onClick={handleSendMessage} className="chat-send-button">
                Send
              </button>
            </div>
          </>
        ) : (
          <div className="no-chat-selected">
            <h2>Select a conversation to start chatting</h2>
          </div>
        )}
      </div>
    </div>
  );
};

export default Chat;
