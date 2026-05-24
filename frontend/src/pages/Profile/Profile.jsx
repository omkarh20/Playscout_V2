import React, { useContext, useEffect, useState } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { StoreContext } from '../../context/storeContextInstance';
import { avatars, getAvatarLabel } from '../../constants/avatars';
import './Profile.css';

const Profile = () => {
  const { url, token, setToken, setUserId, setRole, userImage, setUserImage, getImageUrl } = useContext(StoreContext);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    userImage: avatars[0],
    role: ''
  });

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }

    const fetchProfile = async () => {
      try {
        const response = await axios.get(`${url}/user/me`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        const payload = response?.data || {};
        setFormData({
          name: payload.name || '',
          email: payload.email || '',
          password: '',
          userImage: payload.userImage || avatars[0],
          role: payload.role || ''
        });
      } catch (error) {
        toast.error(error?.response?.data?.message || 'Unable to load profile details.');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [token, url]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const onSave = async (event) => {
    event.preventDefault();
    if (!token) {
      toast.info('Please login to edit your profile.');
      return;
    }

    setSaving(true);
    try {
      const payload = {
        name: formData.name,
        email: formData.email,
        userImage: formData.userImage
      };

      if (formData.password.trim()) {
        payload.password = formData.password.trim();
      }

      const response = await axios.put(`${url}/user/me`, payload, {
        headers: { Authorization: `Bearer ${token}` }
      });

      const updated = response?.data || {};
      const refreshedToken = updated.token || token;
      const updatedAvatar = updated.userImage || formData.userImage;

      localStorage.setItem('token', refreshedToken);
      localStorage.setItem('userImage', updatedAvatar);
      if (updated.userId) {
        localStorage.setItem('userId', updated.userId);
      }
      if (updated.role) {
        localStorage.setItem('role', updated.role);
      }

      setToken(refreshedToken);
      if (updated.userId) {
        setUserId(updated.userId);
      }
      if (updated.role) {
        setRole(updated.role);
      }
      setUserImage(updatedAvatar);

      setFormData((prev) => ({
        ...prev,
        password: '',
        userImage: updatedAvatar,
        role: updated.role || prev.role
      }));

      toast.success('Profile updated successfully.');
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Unable to update profile.');
    } finally {
      setSaving(false);
    }
  };

  if (!token) {
    return (
      <div className='profile-page'>
        <div className='profile-card'>
          <h2>Profile</h2>
          <p>Please login to view your profile.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className='profile-page'>
        <div className='profile-card'>
          <h2>Profile</h2>
          <p>Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className='profile-page'>
      <form className='profile-card' onSubmit={onSave}>
        <h2>My Profile</h2>
        <p>Update your account details and avatar.</p>

        <div className='profile-avatar-preview'>
          <img src={getImageUrl(formData.userImage || userImage)} alt='Selected avatar' />
        </div>

        <label htmlFor='profile-name'>Name</label>
        <input id='profile-name' name='name' value={formData.name} onChange={onChange} required />

        <label htmlFor='profile-email'>Email</label>
        <input id='profile-email' type='email' name='email' value={formData.email} onChange={onChange} required />

        <label htmlFor='profile-role'>Role</label>
        <input id='profile-role' value={formData.role} disabled />

        <label>Avatar</label>
        <div className='avatar-picker-grid'>
          {avatars.map((avatarPath) => (
            <button
              key={avatarPath}
              type='button'
              className={`avatar-picker-item ${formData.userImage === avatarPath ? 'selected' : ''}`}
              onClick={() => setFormData((prev) => ({ ...prev, userImage: avatarPath }))}
              aria-label={`Select ${getAvatarLabel(avatarPath)}`}
            >
              <img src={getImageUrl(avatarPath)} alt='' />
            </button>
          ))}
        </div>

        <label htmlFor='profile-password'>New Password (optional)</label>
        <input
          id='profile-password'
          type='password'
          name='password'
          value={formData.password}
          onChange={onChange}
          minLength={6}
          placeholder='Leave empty to keep current password'
        />

        <button type='submit' disabled={saving} className='profile-update-submit'>
          {saving ? 'Saving...' : 'Save Changes'}
        </button>
      </form>
    </div>
  );
};

export default Profile;