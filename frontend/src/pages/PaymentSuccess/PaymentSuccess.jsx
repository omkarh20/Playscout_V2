import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';

const PaymentSuccess = () => {
  const [searchParams] = useSearchParams();
  const sessionId = searchParams.get('session_id');
  const [status, setStatus] = useState('Checking payment status...');
  const navigate = useNavigate();
  const url = import.meta.env.VITE_BACKEND_URL;

  useEffect(() => {
    if (!sessionId) {
      setStatus('No session id provided.');
      return;
    }

    const token = localStorage.getItem('token');
    const authHeader = token ? { Authorization: `Bearer ${token}` } : {};

    const checkSession = async () => {
      try {
        const response = await axios.get(
          `${url}/payments/checkout-session/${sessionId}`,
          { headers: authHeader }
        );
        const paymentStatus = response.data?.status || response.data?.session?.payment_status;

        if (paymentStatus === 'paid' || paymentStatus === 'succeeded' || paymentStatus === 'complete') {
          setStatus('CONFIRMED');
          toast.success('Payment successful. Booking status: CONFIRMED.');
          setTimeout(() => navigate('/upcoming'), 1800);
        } else {
          setStatus(`Payment status: ${paymentStatus || 'unknown'}.`);
          toast.error(`Payment status is ${paymentStatus || 'unknown'}. Please check your stripe dashboard or retry.`);
        }
      } catch (error) {
        console.error('Could not verify payment', error);
        setStatus('Unable to verify payment right now.');
        toast.error('Could not verify payment at the moment.');
      }
    };

    checkSession();
  }, [sessionId, url, navigate]);

  const displayStatus =
    status === 'CONFIRMED'
      ? 'Payment completed successfully. Booking status: CONFIRMED.'
      : status;

  return (
    <div className="payment-result" style={{ padding: '2rem', textAlign: 'center' }}>
      <h2>Payment Result</h2>
      <p>{displayStatus}</p>
    </div>
  );
};

export default PaymentSuccess;
