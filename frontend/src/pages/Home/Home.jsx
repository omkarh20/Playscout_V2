import React, { useEffect } from 'react';
import './Home.css';
import Header from '../../components/Header/Header';
import ExploreSports from '../../components/ExploreSports/ExploreSports';
import ExplorePlayers from '../../components/ExplorePlayers/ExplorePlayers';
import ExploreVenues from '../../components/ExploreVenues/ExploreVenues';

const Home = () => {

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <div className='home'>
      <Header />
      <ExploreSports />
      <ExplorePlayers />
      <ExploreVenues />
    </div>
  );
};

export default Home;