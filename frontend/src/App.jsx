import React, { useState, useEffect } from 'react'
import { Route, Routes } from 'react-router-dom'
import { ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const App = () => {

  return (
    <>
      <div className='app' style={{backgroundColor:'#f1f1f1'}}>
        <ToastContainer />
        <Routes>
        </Routes>
      </div>
    </>
  )
}

export default App