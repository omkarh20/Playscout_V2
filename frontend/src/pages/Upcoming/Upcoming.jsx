import React, { useContext, useEffect, useState } from 'react';
import axios from 'axios';
import './Upcoming.css';
import { StoreContext } from '../../context/storeContextInstance';
import { toast } from 'react-toastify';

const Upcoming = () => {
  const [bookings, setBookings] = useState([]);
  const [plannedGames, setPlannedGames] = useState([]);
  const [requests, setRequests] = useState([]);
  const { url, fetchGameList, fetchVenueList, token, getImageUrl } = useContext(StoreContext);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  const fetchBookings = async () => {
    try {
      const response = await axios.get(`${url}/api/bookings/list-bookings`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setBookings(response?.data?.data || []);
    } catch (error) {
      setBookings([]);
    }
  };

  const fetchPlannedGames = async () => {
    try {
      const response = await axios.get(`${url}/api/game/list-planned-games`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setPlannedGames(response?.data?.data || []);
    } catch (error) {
      setPlannedGames([]);
    }
  };

  const fetchRequests = async () => {
    try {
      const response = await axios.get(`${url}/api/join/get-request`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setRequests(response?.data?.data || []);
    } catch (error) {
      setRequests([]);
    }
  };

  const handleResponse = async (requestId, status) => {
    try {
      const response = await axios.post(
        `${url}/api/join/respond-request`,
        { requestId, status },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response?.data?.success) {
        toast.success(response.data.message);
        setRequests((prevRequests) =>
          prevRequests.map((request) => (request._id === requestId ? { ...request, status } : request))
        );
      } else {
        toast.error(response?.data?.message || 'Failed to update request');
      }
    } catch (error) {
      toast.error('Join request update is unavailable right now.');
    }
  };

  const removeGame = async (gameID) => {
    try {
      const response = await axios.post(`${url}/api/game/remove-game`, { id: gameID });
      if (response?.data?.success) {
        toast.success(response.data.message);
        fetchPlannedGames();
        fetchGameList();
      } else {
        toast.error('Unable to remove game');
      }
    } catch (error) {
      toast.error('Game removal endpoint is unavailable.');
    }
  };

  const cancelBooking = async (bookId) => {
    try {
      const response = await axios.post(
        `${url}/api/bookings/cancel-booking`,
        { id: bookId },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (response?.data?.success) {
        toast.success(response.data.message);
        fetchBookings();
        fetchVenueList();
      } else {
        toast.error('Unable to cancel booking');
      }
    } catch (error) {
      toast.error('Booking cancellation endpoint is unavailable.');
    }
  };

  useEffect(() => {
    if (!token) {
      toast.info('Please login to view upcoming activities.');
      return;
    }

    fetchBookings();
    fetchPlannedGames();
    fetchRequests();
  }, [token]);

  if (!token) {
    return (
      <div className='upcoming-container'>
        <h2>Upcoming</h2>
        <p className='no-data-message'>Login to see your bookings, planned games, and requests.</p>
      </div>
    );
  }

  return (
    <div className='upcoming-container'>
      <h2>Upcoming Games</h2>

      <div>
        <h3>Booked</h3>
        <table className='bookings-table'>
          <thead>
            <tr>
              <th>Preview</th>
              <th>Venue</th>
              <th>Location</th>
              <th>Sport</th>
              <th>Date</th>
              <th>Slot</th>
              <th className='small-column'>Members Joined</th>
              <th className='small-column'>Total Members</th>
              <th>Cancel</th>
            </tr>
          </thead>
          <tbody>
            {bookings.length > 0 ? (
              bookings.map((booking, index) => (
                <tr key={index}>
                  <td className='image-cell'>
                    <img src={getImageUrl(booking.courtImage)} alt={booking.courtName} className='court-image' />
                  </td>
                  <td>{booking.courtName}</td>
                  <td>{booking.courtLocation}</td>
                  <td>{booking.sport}</td>
                  <td>{booking.bookingDate}</td>
                  <td>{booking.slot}</td>
                  <td className='small-column'>{booking.membersJoined}</td>
                  <td className='small-column'>{booking.totalMembers}</td>
                  <td>
                    <p onClick={() => cancelBooking(booking._id)} className='book-cancel'>
                      x
                    </p>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan='9'>No upcoming bookings</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div>
        <h3>Planned</h3>
        <table className='bookings-table'>
          <thead>
            <tr>
              <th>Sport</th>
              <th>Date</th>
              <th>Level</th>
              <th>Court Name</th>
              <th>Location</th>
              <th>Members Joined</th>
              <th>Total Members</th>
              <th>Requests</th>
              <th>Remove</th>
            </tr>
          </thead>
          <tbody>
            {plannedGames.length > 0 ? (
              plannedGames.map((game, index) => {
                const gameRequests = requests.filter((request) => request.gameId === game._id);

                return (
                  <tr key={index}>
                    <td>
                      <img src={getImageUrl(game.sportIcon)} alt='Sport' className='icon-image' />
                    </td>
                    <td>{(game.date || '').split('T')[0] || game.date}</td>
                    <td>{game.level}</td>
                    <td>{game.courtName}</td>
                    <td>{game.location}</td>
                    <td>{game.membersJoined}</td>
                    <td>{game.totalMembers}</td>
                    <td>
                      {gameRequests.length > 0 ? (
                        gameRequests.map((request, i) => (
                          <div key={i} className='request-details'>
                            <p>
                              Request from:
                              <br />
                              Name: {request.senderId?.name}
                              <br />
                              Mail: {request.senderId?.email}
                              <br />
                              Status: {request.status}
                              <br />
                              {request.status === 'pending' ? (
                                <>
                                  <span className='accept-request' onClick={() => handleResponse(request._id, 'accepted')}>
                                    √
                                  </span>
                                  <span className='book-cancel' onClick={() => handleResponse(request._id, 'declined')}>
                                    x
                                  </span>
                                </>
                              ) : null}
                            </p>
                          </div>
                        ))
                      ) : (
                        <p className='request-details'>No requests</p>
                      )}
                    </td>
                    <td>
                      <p onClick={() => removeGame(game._id)} className='book-cancel'>
                        x
                      </p>
                    </td>
                  </tr>
                );
              })
            ) : (
              <tr>
                <td colSpan='9'>No planned games</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Upcoming;
