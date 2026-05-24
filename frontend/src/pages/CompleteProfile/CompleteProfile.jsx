import React, { useContext, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { StoreContext } from '../../context/storeContextInstance';
import axios from 'axios';
import { toast } from 'react-toastify';
import { avatars, getAvatarLabel } from '../../constants/avatars';
import './CompleteProfile.css';

const CompleteProfile = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { url, setToken, setRole, setUserId, setUserImage: setContextUserImage, getImageUrl } = useContext(StoreContext);

  const initialEmail = searchParams.get('email') || '';
  const initialName = searchParams.get('name') || '';

  const [name, setName] = useState(initialName);
  const [email, setEmail] = useState(initialEmail);
  const [password, setPassword] = useState('');
  const [selectedRole, setSelectedRole] = useState('PLAYER');
  const [selectedAvatar, setSelectedAvatar] = useState(avatars[0]);
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);

    try {
      const response = await axios.post(`${url}/auth/oauth-register`, {
        name,
        email,
        password,
        role: selectedRole,
        userImage: selectedAvatar
      });

      if (!response?.data?.token) {
        toast.error('Profile completion failed. Please try again.');
        return;
      }

      localStorage.setItem('token', response.data.token);
      localStorage.setItem('role', response.data.role || 'PLAYER');
      localStorage.setItem('userId', response.data.userId || '');
      localStorage.setItem('userImage', response.data.userImage || selectedAvatar);

      setToken(response.data.token);
      setRole(response.data.role || 'PLAYER');
      setUserId(response.data.userId || '');
      setContextUserImage(response.data.userImage || selectedAvatar);

      toast.success('Profile completed successfully');
      navigate('/');
    } catch (error) {
      const msg = error?.response?.data?.message || 'Unable to complete profile.';
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className='complete-profile-page'>
      <form className='complete-profile-card' onSubmit={handleSubmit}>
        <h2>Complete Your Profile</h2>
        <p>Finish your PlayScout account setup to continue.</p>

        <label htmlFor='cp-name'>Name</label>
        <input id='cp-name' type='text' value={name} onChange={(e) => setName(e.target.value)} required />

        <label htmlFor='cp-email'>Email</label>
        <input id='cp-email' type='email' value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label htmlFor='cp-password'>Set Password</label>
        <input
          id='cp-password'
          type='password'
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
        />

        <label htmlFor='cp-role'>Choose Role</label>
        <select
          id='cp-role'
          value={selectedRole}
          onChange={(e) => setSelectedRole(e.target.value)}
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
              className={`avatar-picker-item ${selectedAvatar === avatarPath ? 'selected' : ''}`}
              onClick={() => setSelectedAvatar(avatarPath)}
              aria-label={`Select ${getAvatarLabel(avatarPath)}`}
            >
              <img src={getImageUrl(avatarPath)} alt='' />
            </button>
          ))}
        </div>

        <button type='submit' disabled={saving}>
          {saving ? 'Saving...' : 'Complete Profile'}
        </button>
      </form>
    </div>
  );
};

export default CompleteProfile;
