import React, { useState, useEffect, useContext } from 'react';
import { Plus, List, UploadCloud } from 'lucide-react';
import './FacilityManagement.css';
import axios from 'axios';
import { StoreContext } from '../../context/storeContextInstance';
import { toast } from 'react-toastify';

const FacilityManagement = () => {
  const { url, token, getImageUrl } = useContext(StoreContext);
  const [showAddForm, setShowAddForm] = useState(false);
  const [venues, setVenues] = useState([]);
  const [venueData, setVenueData] = useState({
    courtName: '',
    sport: '',
    courtLocation: '',
    courtsAvailable: '',
    price: '',
    courtImage: null,
  });
  const [imagePreview, setImagePreview] = useState(null);

  const fetchVenues = async () => {
    if (!token) {
      setVenues([]);
      return;
    }

    try {
      const response = await axios.get(`${url}/venues/mine`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setVenues(response?.data?.data || []);
    } catch (error) {
      toast.error('Failed to fetch venues.');
      setVenues([]);
    }
  };

  useEffect(() => {
    fetchVenues();
  }, [url, token]);

  useEffect(() => {
    return () => {
      if (imagePreview) {
        URL.revokeObjectURL(imagePreview);
      }
    };
  }, [imagePreview]);

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (!file) {
      return;
    }

    if (imagePreview) {
      URL.revokeObjectURL(imagePreview);
    }

    setImagePreview(URL.createObjectURL(file));
    setVenueData((prev) => ({ ...prev, courtImage: file }));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setVenueData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      toast.error('Please login first.');
      return;
    }

    // Validation
    if (!venueData.courtName.trim()) {
      toast.error('Court name is required.');
      return;
    }
    if (!venueData.sport.trim()) {
      toast.error('Sport is required.');
      return;
    }
    if (!venueData.courtLocation.trim()) {
      toast.error('Court location is required.');
      return;
    }
    const courtsAvailableNum = Number(venueData.courtsAvailable);
    if (isNaN(courtsAvailableNum) || courtsAvailableNum <= 0) {
      toast.error('Courts available must be a number greater than 0.');
      return;
    }
    const priceNum = Number(venueData.price);
    if (isNaN(priceNum) || priceNum < 0) {
      toast.error('Price must be a number 0 or greater.');
      return;
    }
    if (!venueData.courtImage) {
      toast.error('Court image is required.');
      return;
    }

    const formData = new FormData();
    formData.append('courtName', venueData.courtName.trim());
    formData.append('sport', venueData.sport.trim());
    formData.append('courtLocation', venueData.courtLocation.trim());
    formData.append('courtsAvailable', courtsAvailableNum);
    formData.append('price', priceNum);
    formData.append('court-image', venueData.courtImage);

    try {
      const response = await axios.post(`${url}/venues`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });

      if (response?.data?.success) {
        toast.success(response.data.message || 'Venue added successfully');
        setVenueData({
          courtName: '',
          sport: '',
          courtLocation: '',
          courtsAvailable: '',
          price: '',
          courtImage: null
        });
        setImagePreview(null);
        setShowAddForm(false);
        fetchVenues();
      } else {
        toast.error(response?.data?.message || 'Unable to add venue');
      }
    } catch (error) {
      const serverMessage = error?.response?.data?.message;
      if (serverMessage) {
        toast.error(serverMessage);
      } else {
        toast.error('Venue add endpoint error or unauthorized.');
      }
      console.error("Error adding venue:", error);
    }
  };

  const removeVenue = async (venueID) => {
    if (!token) {
      toast.error('Please login first.');
      return;
    }

    try {
      const response = await axios.delete(
        `${url}/venues/${venueID}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response?.data?.success) {
        toast.success(response.data.message || 'Venue removed');
        fetchVenues();
      } else {
        toast.error(response?.data?.message || 'Unable to remove venue');
      }
    } catch (error) {
      toast.error('Venue remove endpoint error or unauthorized.');
    }
  };

  return (
    <div className="facility-management-container">
      <div className="button-group">
        <button
          onClick={() => setShowAddForm(false)}
          className={`custom-button ${!showAddForm ? 'active' : ''}`}
        >
          <List className="button-icon" />
          List Venues
        </button>
        <button
          onClick={() => setShowAddForm(true)}
          className={`custom-button ${showAddForm ? 'active' : ''}`}
        >
          <Plus className="button-icon" />
          Add Venue
        </button>
      </div>

      {showAddForm ? (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Add New Venue</h2>
          </div>
          <div className="card-content">
            <form onSubmit={handleSubmit} className="venue-form">
              <div className="form-grid">
                <div className="form-group">
                  <label>Court Name</label>
                  <input
                    name="courtName"
                    value={venueData.courtName}
                    onChange={handleInputChange}
                    placeholder="Enter court name"
                    required
                    className="input-court"
                  />
                </div>
                <div className="form-group">
                  <label>Sport</label>
                  <input
                    name="sport"
                    value={venueData.sport}
                    onChange={handleInputChange}
                    placeholder="Enter sport type"
                    required
                    className="input"
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Court Location</label>
                <input
                  name="courtLocation"
                  value={venueData.courtLocation}
                  onChange={handleInputChange}
                  placeholder="Enter court location"
                  required
                  className="input"
                />
              </div>

              <div className="form-grid">
                <div className="form-group">
                  <label>Courts Available</label>
                  <input
                    type="number"
                    name="courtsAvailable"
                    value={venueData.courtsAvailable}
                    onChange={handleInputChange}
                    placeholder="Number of courts"
                    min="1"
                    required
                    className="input"
                  />
                </div>
                <div className="form-group">
                  <label>Price/hr</label>
                  <input
                    type="number"
                    name="price"
                    value={venueData.price}
                    onChange={handleInputChange}
                    placeholder="Enter price"
                    min="0"
                    required
                    className="input"
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Court Image</label>
                <div className="image-upload-container">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageUpload}
                    className="hidden"
                    id="court-image-upload"
                    required
                  />
                  <label htmlFor="court-image-upload" className="upload-label">
                    {imagePreview ? (
                      <img src={imagePreview} alt="Preview" className="image-preview" />
                    ) : (
                      <div className="upload-placeholder">
                        <UploadCloud className="upload-icon" />
                        <span>Upload court image</span>
                      </div>
                    )}
                  </label>
                </div>
              </div>

              <button type="submit" className="submit-button">
                Add Venue
              </button>
            </form>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">My Venues</h2>
          </div>
          <div className="card-content">
            {venues.length > 0 ? (
              <table className="venue-table">
                <thead>
                  <tr>
                    <th>Court Name</th>
                    <th>Sport</th>
                    <th>Location</th>
                    <th>Courts Available</th>
                    <th>Price/hr</th>
                    <th>Rating</th>
                    <th>Image</th>
                    <th>Remove</th>
                  </tr>
                </thead>
                <tbody>
                  {venues.map((venue) => (
                    <tr key={venue._id || venue.id}>
                      <td>{venue.courtName}</td>
                      <td>{venue.sport}</td>
                      <td>{venue.courtLocation}</td>
                      <td>{venue.courtsAvailable}</td>
                      <td>{venue.price}</td>
                      <td>{venue.rating}</td>
                      <td>
                        <img
                          src={getImageUrl(venue.courtImage)}
                          alt="Court"
                          className="court-image"
                        />
                      </td>
                      <td>
                        <p
                          onClick={() => removeVenue(venue._id || venue.id)}
                          className="remove-icon"
                        >
                          x
                        </p>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="empty-state">No venues added yet</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default FacilityManagement;
