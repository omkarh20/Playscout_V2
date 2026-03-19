import React, { useState, useEffect } from 'react'
import { Route, Routes } from 'react-router-dom'
import { ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Home from './pages/Home/Home'
import Footer from './components/Footer/Footer';
import Navbar from './components/Navbar/Navbar';
import Book from './pages/Booking/Booking';
import Facility from './pages/Facility/Facility';
import Meet from './pages/Meet/Meet';

const App = () => {

  return (
    <>
      <div className='app' style={{backgroundColor:'#f1f1f1'}}>
        <ToastContainer />
        <Navbar />
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/book' element={<Book />} />
          <Route path='/meet' element={<Meet />} />
          <Route path='/facility/:id' element={<Facility />} />
        </Routes>
        <Footer />
      </div>
    </>
  )
}

export default App