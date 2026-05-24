import React, { useContext, useEffect, useState } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import './PaymentHistory.css';
import { StoreContext } from '../../context/storeContextInstance';

const PaymentHistory = () => {
  const { url, token } = useContext(StoreContext);
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  const formatAmount = (amount, currency) => {
    if (amount == null) return '-';
    const curr = String(currency || '').toLowerCase();
    const value = curr ? amount / 100 : amount;
    const displayCurrency = curr ? curr.toUpperCase() : '';
    return `${value} ${displayCurrency}`.trim();
  };

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }

    const fetchHistory = async () => {
      try {
        const response = await axios.get(`${url}/payments/history`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setPayments(response?.data?.data || []);
      } catch (error) {
        toast.error(error?.response?.data?.message || 'Unable to load payment history.');
        setPayments([]);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [token, url]);

  if (!token) {
    return (
      <div className='payment-history-page'>
        <div className='payment-history-card'>
          <h2>Payment History</h2>
          <p>Please login to view your payments.</p>
        </div>
      </div>
    );
  }

  return (
    <div className='payment-history-page'>
      <div className='payment-history-card'>
        <h2>Payment History</h2>
        {loading ? (
          <p>Loading payments...</p>
        ) : payments.length > 0 ? (
          <table className='payment-history-table'>
            <thead>
              <tr>
                <th>Date</th>
                <th>Court</th>
                <th>Slot</th>
                <th>Amount</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((payment) => (
                <tr key={payment.id}>
                  <td>{(payment.bookingDate || '').split('T')[0] || payment.bookingDate}</td>
                  <td>{payment.courtName}</td>
                  <td>{payment.bookingSlot}</td>
                  <td>{formatAmount(payment.amount, payment.currency)}</td>
                  <td>{payment.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No payments found.</p>
        )}
      </div>
    </div>
  );
};

export default PaymentHistory;
