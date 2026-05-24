import React, { useContext, useState } from 'react';
import './LoginPopup.css';
import { assets } from '../../assets/assets';
import { StoreContext } from '../../context/storeContextInstance';
import axios from 'axios';
import { toast } from 'react-toastify';
import { avatars, getAvatarLabel } from '../../constants/avatars';

const LoginPopup = ({ setShowLogin }) => {
  const { url, setToken, setRole, setUserId, setUserImage, getImageUrl } = useContext(StoreContext);

  const [mode, setMode] = useState('Login');
  const [data, setData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'PLAYER',
    userImage: avatars[0]
  });

  const onChangeHandler = (event) => {
    const { name, value } = event.target;
    setData((prev) => ({ ...prev, [name]: value }));
  };

  const saveSession = (payload, fallbackAvatar) => {
    const resolvedToken = payload?.token || '';
    const resolvedRole = payload?.role || 'PLAYER';
    const resolvedUserId = payload?.userId || '';
    const resolvedAvatar = payload?.userImage || fallbackAvatar || localStorage.getItem('userImage') || 'avatars/m_avatar1.png';

    localStorage.setItem('token', resolvedToken);
    localStorage.setItem('role', resolvedRole);
    if (resolvedUserId) {
      localStorage.setItem('userId', resolvedUserId);
    }
    localStorage.setItem('userImage', resolvedAvatar);

    setToken(resolvedToken);
    setRole(resolvedRole);
    setUserId(resolvedUserId);
    setUserImage(resolvedAvatar);
  };

  const onSubmit = async (event) => {
    event.preventDefault();

    const endpoint = mode === 'Login' ? '/auth/login' : '/auth/register';
    const selectedAvatar = data.userImage || avatars[0];

    const payload =
      mode === 'Login'
        ? {
            email: data.email,
            password: data.password
          }
        : {
            name: data.name,
            email: data.email,
            password: data.password,
            role: data.role,
            userImage: selectedAvatar
          };

    try {
      const response = await axios.post(url + endpoint, payload);
      if (!response?.data?.token) {
        toast.error('Authentication failed. Please try again.');
        return;
      }

      saveSession(response.data, selectedAvatar);
      setShowLogin(false);
      toast.success(mode === 'Login' ? 'Logged in successfully' : 'Account created successfully');
    } catch (error) {
      const message = error?.response?.data?.message || 'Unable to authenticate. Please check your details.';
      toast.error(message);
    }
  };

  const startGoogleOAuth = () => {
    if (import.meta.env.VITE_OAUTH_ENABLED !== 'true') {
      toast.info('Google OAuth is currently disabled for this environment.');
      return;
    }
    window.location.href = `${url}/oauth2/authorization/google`;
  };

  return (
    <div className='login-popup'>
      <form onSubmit={onSubmit} className='login-popup-container'>
        <div className='login-popup-logo'>
          <img src={assets.logo} id='login-logo' alt='PlayScout' />
          <img onClick={() => setShowLogin(false)} src={assets.cross_icon} alt='Close' />
        </div>

        <div className='login-popup-title'>
          <h2>{mode}</h2>
        </div>

        <div className='login-popup-inputs'>
          {mode === 'Login' ? null : (
            <div className='name-input'>
              <img src={assets.profile_icon} alt='' />
              <input
                name='name'
                onChange={onChangeHandler}
                value={data.name}
                type='text'
                placeholder='Your Name'
                required
              />

              <label htmlFor='signup-role'>Choose Role</label>
              <select
                id='signup-role'
                name='role'
                value={data.role}
                onChange={onChangeHandler}
                required
              >
                <option value='PLAYER'>Player</option>
                <option value='FACILITY_MANAGER'>Facility Manager</option>
              </select>

              <label>Choose Avatar</label>
              <div className='avatar-picker-grid'>
                {avatars.map((avatarPath) => (
                  <button
                    key={avatarPath}
                    type='button'
                    className={`avatar-picker-item ${data.userImage === avatarPath ? 'selected' : ''}`}
                    onClick={() => setData((prev) => ({ ...prev, userImage: avatarPath }))}
                    aria-label={`Select ${getAvatarLabel(avatarPath)}`}
                  >
                    <img src={getImageUrl(avatarPath)} alt='' />
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className='email-input'>
            <img src={assets.email_icon} alt='' />
            <input
              name='email'
              onChange={onChangeHandler}
              value={data.email}
              type='email'
              placeholder='Your Email'
              required
            />
          </div>

          <div className='password-input'>
            <img src={assets.password_icon} alt='' />
            <input
              name='password'
              onChange={onChangeHandler}
              value={data.password}
              type='password'
              placeholder='Password'
              required
            />
          </div>
        </div>

        <button type='submit'>{mode === 'Sign Up' ? 'Create account' : 'Login'}</button>

        <button type='button' className='oauth-button' onClick={startGoogleOAuth}>
          Continue with Google
        </button>

        <div className='login-popup-condition'>
          <input type='checkbox' required />
          <p>By continuing, I agree to the terms of use and privacy policy</p>
        </div>

        {mode === 'Login' ? (
          <p>
            Create a new account? <span onClick={() => setMode('Sign Up')}>Click here</span>
          </p>
        ) : (
          <p>
            Already have an account? <span onClick={() => setMode('Login')}>Click here</span>
          </p>
        )}
      </form>
    </div>
  );
};

export default LoginPopup;
