import React, { useContext, useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { RefreshCw } from 'lucide-react';
import './Upcoming.css';
import { StoreContext } from '../../context/storeContextInstance';
import { toast } from 'react-toastify';

const Upcoming = () => {
  const [bookings, setBookings] = useState([]);
  const [plannedGames, setPlannedGames] = useState([]);
  const [expandedGameId, setExpandedGameId] = useState('');
  const [expandedJoinedGameId, setExpandedJoinedGameId] = useState('');
  const [requestActionLoadingId, setRequestActionLoadingId] = useState('');
  const [refreshingPageData, setRefreshingPageData] = useState(false);
  const [paymentLoadingId, setPaymentLoadingId] = useState('');
  const {
    url,
    fetchGameList,
    fetchVenueList,
    token,
    role,
    getImageUrl,
    incomingJoinRequests,
    sentJoinRequests,
    fetchIncomingJoinRequests,
    fetchSentJoinRequests,
    acceptJoinRequest,
    rejectJoinRequest,
    cancelSentJoinRequest
  } = useContext(StoreContext);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  const fetchBookings = async () => {
    try {
      const response = await axios.get(`${url}/api/bookings`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setBookings(response?.data?.data || []);
    } catch (error) {
      setBookings([]);
    }
  };

  const fetchPlannedGames = async () => {
    try {
      const response = await axios.get(`${url}/api/games/me`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setPlannedGames(response?.data?.data || []);
    } catch (error) {
      setPlannedGames([]);
    }
  };

  const fetchJoinRequests = async () => {
    await Promise.all([fetchIncomingJoinRequests(), fetchSentJoinRequests()]);
  };

  const removeGame = async (gameID) => {
    try {
      const response = await axios.delete(`${url}/api/games/${gameID}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response?.data?.success) {
        toast.success(response?.data?.message || 'Game cancelled successfully');
        setPlannedGames((prev) => prev.filter((game) => game._id !== gameID));
        await Promise.all([fetchPlannedGames(), fetchGameList({ force: true })]);
      } else {
        toast.error(response?.data?.message || 'Unable to cancel planned game');
      }
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Game cancellation endpoint is unavailable.');
    }
  };

  const cancelBooking = async (bookId) => {
    try {
      const response = await axios.patch(
        `${url}/api/bookings/${bookId}`,
        { status: 'CANCELLED' },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (response?.data?.success) {
        if (response.data?.refundId) {
          toast.success(`${response.data.message} (Refund: ${response.data.refundId})`);
        } else {
          toast.success(response.data.message);
        }
        setBookings((prev) => prev.filter((booking) => booking.id !== bookId));
        await fetchBookings();
        await fetchVenueList({ force: true });
      } else {
        toast.error('Unable to cancel booking');
      }
    } catch (error) {
      toast.error('Booking cancellation endpoint is unavailable.');
    }
  };

  const handlePayNow = async (booking) => {
    if (!booking?.id) {
      return;
    }
    try {
      setPaymentLoadingId(booking.id);
      const response = await axios.post(
        `${url}/api/payments/checkout-session/booking/${booking.id}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const redirectUrl = response?.data?.url;
      if (redirectUrl) {
        window.location.href = redirectUrl;
      } else {
        toast.error('Unable to start payment.');
      }
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Unable to start payment.');
    } finally {
      setPaymentLoadingId('');
    }
  };

  const handleAcceptRequest = async (requestId) => {
    try {
      setRequestActionLoadingId(requestId);
      const response = await acceptJoinRequest(requestId);
      if (response?.data?.success) {
        toast.success(response?.data?.message || 'Join request accepted');
        await Promise.all([fetchJoinRequests(), fetchPlannedGames(), fetchGameList({ force: true })]);
      } else {
        toast.error(response?.data?.message || 'Unable to accept request');
      }
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Unable to accept request');
    } finally {
      setRequestActionLoadingId('');
    }
  };

  const handleRejectRequest = async (requestId) => {
    try {
      setRequestActionLoadingId(requestId);
      const response = await rejectJoinRequest(requestId);
      if (response?.data?.success) {
        toast.success(response?.data?.message || 'Join request rejected');
        await fetchJoinRequests();
      } else {
        toast.error(response?.data?.message || 'Unable to reject request');
      }
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Unable to reject request');
    } finally {
      setRequestActionLoadingId('');
    }
  };

  const handleCancelSentRequest = async (requestId) => {
    try {
      setRequestActionLoadingId(requestId);
      const response = await cancelSentJoinRequest(requestId);
      if (response?.data?.success) {
        toast.success(response?.data?.message || 'Join request cancelled');
        await fetchSentJoinRequests();
      } else {
        toast.error(response?.data?.message || 'Unable to cancel join request');
      }
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Unable to cancel join request');
    } finally {
      setRequestActionLoadingId('');
    }
  };

  const handleRefreshUpcoming = async () => {
    setRefreshingPageData(true);
    await Promise.all([fetchBookings(), fetchPlannedGames(), fetchJoinRequests()]);
    setRefreshingPageData(false);
  };

  useEffect(() => {
    if (!token) {
      toast.info('Please login to view upcoming activities.');
      return;
    }

    fetchBookings();
    fetchPlannedGames();
    fetchJoinRequests();
  }, [token]);

  useEffect(() => {
    if (!token) {
      return undefined;
    }

    const poller = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchJoinRequests();
      }
    }, 30000);

    return () => clearInterval(poller);
  }, [token]);

  const pendingIncomingRequests = useMemo(
    () => incomingJoinRequests.filter((request) => request.status === 'PENDING'),
    [incomingJoinRequests]
  );

  const acceptedIncomingRequests = useMemo(
    () => incomingJoinRequests.filter((request) => request.status === 'ACCEPTED'),
    [incomingJoinRequests]
  );

  const pendingIncomingByGame = useMemo(() => {
    return pendingIncomingRequests.reduce((accumulator, request) => {
      const gameId = request.gameID;
      if (!accumulator[gameId]) {
        accumulator[gameId] = [];
      }
      accumulator[gameId].push(request);
      return accumulator;
    }, {});
  }, [pendingIncomingRequests]);

  const acceptedIncomingByGame = useMemo(() => {
    return acceptedIncomingRequests.reduce((accumulator, request) => {
      const gameId = request.gameID;
      if (!accumulator[gameId]) {
        accumulator[gameId] = [];
      }
      accumulator[gameId].push(request);
      return accumulator;
    }, {});
  }, [acceptedIncomingRequests]);

  const expandedGameRequests = pendingIncomingByGame[expandedGameId] || [];
  const expandedJoinedMembers = acceptedIncomingByGame[expandedJoinedGameId] || [];
  const expandedJoinedGame = plannedGames.find((game) => game._id === expandedJoinedGameId);
  const expandedPreAddedCount = Math.max((expandedJoinedGame?.membersJoined || 0) - expandedJoinedMembers.length, 0);

  const parseBookingStart = (bookingDate, bookingSlot) => {
    if (!bookingDate || !bookingSlot) return null;
    const dateParts = bookingDate.includes('-') ? bookingDate.split('-') : [];
    let dateObj = null;
    if (dateParts.length === 3) {
      const [p1, p2, p3] = dateParts;
      if (p1.length === 4) {
        dateObj = new Date(`${p1}-${p2}-${p3}T00:00:00`);
      } else {
        dateObj = new Date(`${p3}-${p2}-${p1}T00:00:00`);
      }
    }
    if (Number.isNaN(dateObj?.getTime())) {
      return null;
    }
    const slotStart = bookingSlot.split('-')[0]?.trim();
    if (!slotStart) return dateObj;
    const [hours, minutes] = slotStart.split(':').map((value) => parseInt(value, 10));
    if (Number.isNaN(hours)) return dateObj;
    dateObj.setHours(hours, Number.isNaN(minutes) ? 0 : minutes, 0, 0);
    return dateObj;
  };

  const canPayNow = (booking) => {
    if (!booking || booking.status !== 'PENDING') return false;
    const start = parseBookingStart(booking.bookingDate, booking.bookingSlot || booking.slot);
    if (!start) return true;
    return new Date() < start;
  };

  if (!token) {
    return (
      <div className='upcoming-container'>
        <h2>Upcoming</h2>
        <p className='no-data-message'>Login to see your bookings, planned games, and requests.</p>
      </div>
    );
  }

  if (role === 'FACILITY_MANAGER' || role === 'ADMIN') {
    return (
      <div className='upcoming-container'>
        <h2>Upcoming</h2>
        <p className='no-data-message'>Access denied</p>
      </div>
    );
  }

  return (
    <div className='upcoming-container'>
      <div className='upcoming-header'>
        <h2>Upcoming Games</h2>
        <button
          className='upcoming-refresh-btn'
          onClick={handleRefreshUpcoming}
          disabled={refreshingPageData}
          aria-label='Refresh upcoming data'
          title='Refresh upcoming data'
        >
          <RefreshCw size={18} className={refreshingPageData ? 'spin-icon' : ''} />
        </button>
      </div>

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
              <th className='small-column'>Status</th>
              <th className='small-column'>Refund</th>
              <th className='small-column'>Payment</th>
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
                  <td>{(booking.bookingDate || '').split('T')[0] || booking.bookingDate}</td>
                  <td>{booking.bookingSlot || booking.slot}</td>
                  <td className='small-column'>
                    {booking.status === 'CONFIRMED' ? 'BOOKED' : booking.status}
                  </td>
                  <td className='small-column'>{booking.refundStatus || '—'}</td>
                  <td className='small-column'>
                    {booking.paymentIntentId ? (
                      <span className='paid-badge'>PAID</span>
                    ) : canPayNow(booking) ? (
                      <button
                        type='button'
                        className='pay-now-btn'
                        onClick={() => handlePayNow(booking)}
                        disabled={paymentLoadingId === booking.id}
                      >
                        {paymentLoadingId === booking.id ? '...' : 'Pay now'}
                      </button>
                    ) : (
                      <span className='muted-text'>-</span>
                    )}
                  </td>
                  <td>
                    <p onClick={() => cancelBooking(booking.id)} className='book-cancel'>
                      x
                    </p>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan='10'>No upcoming bookings</td>
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
              <th>Venue</th>
              <th>Location</th>
              <th>Members Joined</th>
              <th>Total Members</th>
              <th>Requests</th>
              <th>Cancel</th>
            </tr>
          </thead>
          <tbody>
            {plannedGames.length > 0 ? (
              plannedGames.map((game, index) => {
                return (
                  <tr key={index}>
                    <td>
                      <img src={getImageUrl(game.sportIcon)} alt='Sport' className='icon-image' />
                    </td>
                    <td>{game.date}</td>
                    <td>{game.level}</td>
                    <td>{game.courtName}</td>
                    <td>{game.location}</td>
                    <td>
                      <div className='members-joined-cell'>
                        <span>{game.membersJoined}</span>
                        <button
                          type='button'
                          className='show-members-btn'
                          onClick={() =>
                            setExpandedJoinedGameId((previous) => (previous === game._id ? '' : game._id))
                          }
                        >
                          {expandedJoinedGameId === game._id ? 'Hide' : 'Show'}
                        </button>
                      </div>
                    </td>
                    <td>{game.totalMembers}</td>
                    <td>
                      {(pendingIncomingByGame[game._id] || []).length > 0 ? (
                        <button
                          type='button'
                          className='request-toggle-btn'
                          onClick={() => setExpandedGameId((previous) => (previous === game._id ? '' : game._id))}
                        >
                          {(pendingIncomingByGame[game._id] || []).length} pending
                        </button>
                      ) : (
                        <span className='muted-text'>No requests</span>
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

        {expandedGameId && (
          <div className='planned-subsection'>
            <h4>Incoming Requests For Selected Game</h4>
            <table className='bookings-table'>
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Email</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {expandedGameRequests.length > 0 ? (
                  expandedGameRequests.map((request) => (
                    <tr key={request._id}>
                      <td>{request.senderName}</td>
                      <td>{request.senderEmail}</td>
                      <td>
                        <span className='request-status pending'>PENDING</span>
                      </td>
                      <td>
                        <button
                          type='button'
                          className='request-action-btn accept'
                          onClick={() => handleAcceptRequest(request._id)}
                          disabled={requestActionLoadingId === request._id}
                        >
                          Accept
                        </button>
                        <button
                          type='button'
                          className='request-action-btn reject'
                          onClick={() => handleRejectRequest(request._id)}
                          disabled={requestActionLoadingId === request._id}
                        >
                          Reject
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan='4'>No pending requests for this game</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {expandedJoinedGameId && (
          <div className='planned-subsection'>
            <h4>Joined Members For Selected Game</h4>
            <table className='bookings-table'>
              <thead>
                <tr>
                  <th>Avatar</th>
                  <th>Player</th>
                  <th>Email</th>
                </tr>
              </thead>
              <tbody>
                {expandedJoinedMembers.length > 0 ? (
                  expandedJoinedMembers.map((request) => (
                    <tr key={request._id}>
                      <td>
                        <img src={getImageUrl(request.senderImage)} alt={request.senderName} className='icon-image' />
                      </td>
                      <td>{request.senderName}</td>
                      <td>{request.senderEmail}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan='3'>No database-backed joined members for this game</td>
                  </tr>
                )}
              </tbody>
            </table>

            {expandedPreAddedCount > 0 && (
              <p className='pre-added-summary'>
                {expandedPreAddedCount} pre-added member{expandedPreAddedCount > 1 ? 's' : ''}
              </p>
            )}
          </div>
        )}

        <div className='planned-subsection'>
          <h4>My Sent Join Requests</h4>
          <table className='bookings-table'>
            <thead>
              <tr>
                <th>Sport</th>
                <th>Date</th>
                <th>Slot</th>
                <th>Host</th>
                <th>Status</th>
                <th>Cancel</th>
              </tr>
            </thead>
            <tbody>
              {sentJoinRequests.length > 0 ? (
                sentJoinRequests.map((request) => (
                  <tr key={request._id}>
                    <td>
                      <img src={getImageUrl(request.sportIcon)} alt={request.sportName} className='icon-image' />
                    </td>
                    <td>{request.gameDate}</td>
                    <td>{request.gameSlot}</td>
                    <td>{request.recipientName}</td>
                    <td>
                      <span className={`request-status ${request.status.toLowerCase()}`}>{request.status}</span>
                    </td>
                    <td>
                      {request.status === 'PENDING' ? (
                        <p
                          onClick={() =>
                            requestActionLoadingId !== request._id && handleCancelSentRequest(request._id)
                          }
                          className='book-cancel'
                        >
                          x
                        </p>
                      ) : (
                        <span className='muted-text'>-</span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan='6'>No sent join requests</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Upcoming;
