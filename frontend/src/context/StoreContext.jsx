import { useEffect, useState } from "react";
import axios from "axios";
import { sport_list } from "../assets/assets";
import { StoreContext } from "./storeContextInstance";

const StoreContextProvider = (props) => {
    const getStoredValue = (key) => {
        if (typeof window === "undefined") {
            return "";
        }
        return localStorage.getItem(key) || "";
    };

    const [menu, setMenu] = useState("home");
    const [selectedSport, setSelectedSport] = useState('Select Sport');
    const [selectedMeetSport, setSelectedMeetSport] = useState('Select Sport');
    const [selectedMeetLocation, setSelectedMeetLocation] = useState('Select Location');
    const [startDate, setStartDate] = useState(null);
    
    const url = import.meta.env.VITE_BACKEND_URL;
    const storageBaseUrl = import.meta.env.VITE_SUPABASE_STORAGE_URL;
    const [token, setToken] = useState(() => getStoredValue("token"));
    const [role, setRole] = useState(() => getStoredValue("role"));
    const [userId, setUserId] = useState(() => getStoredValue("userId"));
    const [userImage, setUserImage] = useState(() => getStoredValue("userImage"));
    
    // Data lists
    const [COURT_list, setCourtList] = useState([]);
    const [player_list, setPlayerList] = useState([]);
    const [incomingJoinRequests, setIncomingJoinRequests] = useState([]);
    const [sentJoinRequests, setSentJoinRequests] = useState([]);

    const fetchGameList = async () => {
        try {
            const response = await axios.get(url + "/api/games");
            setPlayerList(response.data.data);
        } catch (error) {
            console.error("Error fetching games:", error);
        }
    }

    const fetchVenueList = async () => {
        try {
            const response = await axios.get(url + "/api/venues");
            setCourtList(response.data.data || []);
        } catch (error) {
            console.error("Error fetching venues:", error);
        }
    }

    const buildAuthHeaders = () => ({
        headers: { Authorization: `Bearer ${token}` }
    });

    const fetchIncomingJoinRequests = async (status = "") => {
        if (!token) {
            setIncomingJoinRequests([]);
            return [];
        }

        try {
            const endpoint = status ? `${url}/api/join-requests/incoming?status=${status}` : `${url}/api/join-requests/incoming`;
            const response = await axios.get(endpoint, buildAuthHeaders());
            const data = response?.data?.data || [];
            setIncomingJoinRequests(data);
            return data;
        } catch (error) {
            console.error("Error fetching incoming join requests:", error);
            setIncomingJoinRequests([]);
            return [];
        }
    };

    const fetchSentJoinRequests = async () => {
        if (!token) {
            setSentJoinRequests([]);
            return [];
        }

        try {
            const response = await axios.get(`${url}/api/join-requests/sent`, buildAuthHeaders());
            const data = response?.data?.data || [];
            setSentJoinRequests(data);
            return data;
        } catch (error) {
            console.error("Error fetching sent join requests:", error);
            setSentJoinRequests([]);
            return [];
        }
    };

    const acceptJoinRequest = async (requestId) => {
        return axios.patch(`${url}/api/join-requests/${requestId}/accept`, {}, buildAuthHeaders());
    };

    const rejectJoinRequest = async (requestId) => {
        return axios.patch(`${url}/api/join-requests/${requestId}/reject`, {}, buildAuthHeaders());
    };

    const cancelSentJoinRequest = async (requestId) => {
        return axios.delete(`${url}/api/join-requests/${requestId}`, buildAuthHeaders());
    };

    const getImageUrl = (path) => {
        if (!path) {
            return "";
        }

        if (/^https?:\/\//i.test(path)) {
            return path;
        }

        if (!storageBaseUrl) {
            return path;
        }

        return `${storageBaseUrl.replace(/\/$/, "")}/${path.replace(/^\//, "")}`;
    };

    useEffect(() => {
        async function loadData() {
            await fetchVenueList();
            await fetchGameList();
        }
        loadData();
    }, []);

    const contextValue = {
        sport_list,
        menu,
        setMenu,
        selectedSport,
        setSelectedSport,
        selectedMeetSport,
        setSelectedMeetSport,
        selectedMeetLocation,
        setSelectedMeetLocation,
        startDate,
        setStartDate,
        url,
        storageBaseUrl,
        getImageUrl,
        token,
        setToken,
        role,
        setRole,
        userId,
        setUserId,
        userImage,
        setUserImage,
        COURT_list,
        setCourtList,
        player_list,
        setPlayerList,
        incomingJoinRequests,
        setIncomingJoinRequests,
        sentJoinRequests,
        setSentJoinRequests,
        fetchGameList,
        fetchVenueList,
        fetchIncomingJoinRequests,
        fetchSentJoinRequests,
        acceptJoinRequest,
        rejectJoinRequest,
        cancelSentJoinRequest,
    };

    return (
        <StoreContext.Provider value={contextValue}>
            {props.children}
        </StoreContext.Provider>
    );
};

export default StoreContextProvider;