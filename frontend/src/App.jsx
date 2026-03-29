import React, { useState } from 'react'
import { Route, Routes } from 'react-router-dom'
import { ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Home from './pages/Home/Home'
import Footer from './components/Footer/Footer';
import Navbar from './components/Navbar/Navbar';
import Book from './pages/Booking/Booking';
import Facility from './pages/Facility/Facility';
import Meet from './pages/Meet/Meet';
import Chat from './pages/Chat/Chat';
import News from './pages/News/News';
import LoginPopup from './components/LoginPopup/LoginPopup';
import Privacy from './pages/Privacy/Privacy';
import Upcoming from './pages/Upcoming/Upcoming';
import Admin from './pages/Admin/Admin';
import OAuthSuccess from './pages/OAuthSuccess/OAuthSuccess';
import CompleteProfile from './pages/CompleteProfile/CompleteProfile';
import Profile from './pages/Profile/Profile';

const App = () => {
  const [showLogin, setShowLogin] = useState(false);

  return (
    <>
      {showLogin ? <LoginPopup setShowLogin={setShowLogin} /> : <></>}
      <div className='app' style={{backgroundColor:'#f1f1f1'}}>
        <ToastContainer position='top-right' style={{ top: '90px', right: '16px' }} />
        <Navbar setShowLogin={setShowLogin} />
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/book' element={<Book />} />
          <Route path='/meet' element={<Meet />} />
          <Route path='/facility/:id' element={<Facility />} />
          <Route path='/chat' element={<Chat />} />
          <Route path='/news' element={<News />} />
          <Route path='/privacy-policy' element={<Privacy />} />
          <Route path='/upcoming' element={<Upcoming />} />
          <Route path='/admin' element={<Admin />} />
          <Route path='/profile' element={<Profile />} />
          <Route path='/oauth-success' element={<OAuthSuccess />} />
          <Route path='/complete-profile' element={<CompleteProfile />} />
        </Routes>
        <Footer />
      </div>
    </>
  )
}

export default App