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
    const listCacheTtlSeconds = Number(import.meta.env.VITE_LIST_CACHE_TTL_SECONDS ?? 120);
    const listCacheTtlMs = Number.isFinite(listCacheTtlSeconds) && listCacheTtlSeconds > 0
        ? listCacheTtlSeconds * 1000
        : 0;
    const [token, setToken] = useState(() => getStoredValue("token"));
    const [role, setRole] = useState(() => getStoredValue("role"));
    const [userId, setUserId] = useState(() => getStoredValue("userId"));
    const [userImage, setUserImage] = useState(() => getStoredValue("userImage"));
    
    // Data lists
    const [COURT_list, setCourtList] = useState([]);
    const [player_list, setPlayerList] = useState([]);
    const [venueLastFetchedAt, setVenueLastFetchedAt] = useState(0);
    const [gameLastFetchedAt, setGameLastFetchedAt] = useState(0);
    const [incomingJoinRequests, setIncomingJoinRequests] = useState([]);
    const [sentJoinRequests, setSentJoinRequests] = useState([]);

    const isCacheFresh = (lastFetchedAt) => {
        if (!lastFetchedAt || listCacheTtlMs <= 0) {
            return false;
        }
        return Date.now() - lastFetchedAt < listCacheTtlMs;
    };

    const fetchGameList = async ({ force = false } = {}) => {
        if (!force && player_list.length > 0 && isCacheFresh(gameLastFetchedAt)) {
            return player_list;
        }

        try {
            const response = await axios.get(url + "/games");
            const data = response?.data?.data || [];
            setPlayerList(data);
            setGameLastFetchedAt(Date.now());
            return data;
        } catch (error) {
            console.error("Error fetching games:", error);
            return player_list;
        }
    }

    const fetchVenueList = async ({ force = false } = {}) => {
        if (!force && COURT_list.length > 0 && isCacheFresh(venueLastFetchedAt)) {
            return COURT_list;
        }

        try {
            const response = await axios.get(url + "/venues");
            const data = response?.data?.data || [];
            setCourtList(data);
            setVenueLastFetchedAt(Date.now());
            return data;
        } catch (error) {
            console.error("Error fetching venues:", error);
            return COURT_list;
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
            const endpoint = status ? `${url}/join-requests/incoming?status=${status}` : `${url}/join-requests/incoming`;
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
            const response = await axios.get(`${url}/join-requests/sent`, buildAuthHeaders());
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
        return axios.patch(`${url}/join-requests/${requestId}/accept`, {}, buildAuthHeaders());
    };

    const rejectJoinRequest = async (requestId) => {
        return axios.patch(`${url}/join-requests/${requestId}/reject`, {}, buildAuthHeaders());
    };

    const cancelSentJoinRequest = async (requestId) => {
        return axios.delete(`${url}/join-requests/${requestId}`, buildAuthHeaders());
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
        venueLastFetchedAt,
        gameLastFetchedAt,
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