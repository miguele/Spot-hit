import type { Player, Playlist, Song } from '../types';

const API_BASE = 'https://api.spotify.com/v1';

const fetchWebApi = async (endpoint: string, method: string, token: string, body?: any) => {
    const res = await fetch(`${API_BASE}/${endpoint}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
        method,
        body: body ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) {
        throw new Error(`Spotify API error: ${res.statusText}`);
    }
    return res.json();
}

export const getUserProfile = async (token: string): Promise<Player> => {
    const data = await fetchWebApi('me', 'GET', token);
    return {
        id: data.id,
        name: data.display_name || data.id,
        avatarUrl: data.images?.[0]?.url,
    };
};

export const getUserPlaylists = async (token: string): Promise<Playlist[]> => {
    const data = await fetchWebApi('me/playlists?limit=50', 'GET', token);
    return data.items.map((p: any): Playlist => ({
        id: p.id,
        name: p.name,
        coverUrl: p.images?.[0]?.url,
        trackCount: p.tracks.total,
    }));
};

export const getPlaylistTracks = async (playlistId: string, token:string): Promise<Song[]> => {
    const data = await fetchWebApi(`playlists/${playlistId}/tracks?fields=items(track(id,name,preview_url,artists(name),album(images,release_date)))`, 'GET', token);
    return data.items
        .filter((item: any) => item.track && item.track.album && item.track.album.release_date && item.track.preview_url) // Ensure preview_url exists
        .map((item: any): Song => ({
            id: item.track.id,
            title: item.track.name,
            artist: item.track.artists.map((a: any) => a.name).join(', '),
            year: parseInt(item.track.album.release_date.substring(0, 4), 10),
            albumArtUrl: item.track.album.images?.[0]?.url,
            previewUrl: item.track.preview_url,
        }))
        .filter((song: Song) => song.id); // Filter out any potential null ID tracks
};