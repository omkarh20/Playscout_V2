import React, { useContext, useEffect, useState } from "react";
import axios from "axios";
import "./News.css";
import { StoreContext } from "../../context/storeContextInstance";

const News = () => {
  const [newsArticles, setNewsArticles] = useState([]);
  const {url} = useContext(StoreContext);

  useEffect(() => {
    axios
      .get(`${url}/news`)
      .then((response) => {
        setNewsArticles(response.data.articles);
      })
      .catch((error) => {
        console.error("Error fetching news:", error);
      });
  }, []);

  return (
    <div className="news-page">
      <h1>What's Trending?</h1>
      <div className="news-list">
        {newsArticles.length === 0 && (
          <p className="news-empty">No news articles available right now. Please try again in a bit.</p>
        )}
        {newsArticles.map((article, index) => (
          <div className="news-item" key={index}>
            <img src={article.image} alt={article.title} />
            <div className="news-content">
              <h3>{article.title}</h3>
              <p>{article.description}</p>
              <a href={article.url} target="_blank" rel="noopener noreferrer">
                Read Full Article
              </a>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default News;