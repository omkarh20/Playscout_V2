import search_icon from './search_icon.png'
import profile_icon from './profile_icon.png'
import logout_icon from './logout_icon.png'
import logo from './logo.png'

import basketball from './basketball.jpg'
import cricket from './cricket.jpg'
import football from './football.jpg'
import badminton from './badminton.png'
import volleyball from './volleyball.jpg'
import squash from './squash.jpg'
import tabletennis from './tabletennis.jpg'
import swimming from './swimming.jpg'
import tennis from './tennis.jpg'

import facebook_icon from './facebook_icon.png'
import twitter_icon from './twitter_icon.png'
import linkedin_icon from './linkedin_icon.png'
import cross_icon from './cross_icon.png'
import email_icon from './email_icon.png'
import password_icon from './password_icon.png'
import location_icon from './location_icon.png'
import calendar_icon from './calendar_icon.png'
import trophy_icon from './trophy_icon.png'
import filter_icon from './filter_icon.png'
import leftIcon from './shuttle_left.png'
import rightIcon from './shuttle_right.png'

export const assets = {
    search_icon,
    profile_icon,
    logout_icon,
    logo,
    facebook_icon,
    twitter_icon,
    linkedin_icon,
    cross_icon,
    email_icon,
    password_icon,
    location_icon,
    calendar_icon,
    trophy_icon,
    filter_icon,
    leftIcon,
    rightIcon
}

export const scroll = (className, scrollAmount) => {
    document.querySelector(`.${className}`).scrollBy({
      left: scrollAmount,
      behavior: 'smooth'
    });
};

export const sport_list = [
    {
        sport_name: "Badminton",
        sport_image: badminton
    },
    {
        sport_name: "Cricket",
        sport_image: cricket
    },
    {
        sport_name: "Volleyball",
        sport_image: volleyball
    },
    {
        sport_name: "Basketball",
        sport_image: basketball
    },
    {
        sport_name: "Football",
        sport_image: football
    },
    {
        sport_name: "Squash",
        sport_image: squash
    },
    {
        sport_name: "Table Tennis",
        sport_image: tabletennis
    },
    {
        sport_name: "Swimming",
        sport_image: swimming
    },
    {
        sport_name: "Tennis",
        sport_image: tennis
    }
]