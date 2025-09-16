/*
import { GoogleGenAI, Type } from "@google/genai";
import type { AIGuessResponse } from '../types';

const ai = new GoogleGenAI({ apiKey: process.env.API_KEY as string });

const responseSchema = {
    type: Type.OBJECT,
    properties: {
        titleMatch: {
            type: Type.BOOLEAN,
            description: "Is the user's guess a correct match for the song title? This can be a partial, misspelled, or descriptive match."
        },
        artistMatch: {
            type: Type.BOOLEAN,
            description: "Is the user's guess a correct match for the artist's name? This can be a partial, misspelled, or descriptive match."
        },
        comment: {
            type: Type.STRING,
            description: "A fun, encouraging, and brief comment about the user's guess, explaining the result. For example, 'You nailed the artist!' or 'Almost! The song was...' or 'Wow, you got both!'"
        },
    },
    required: ["titleMatch", "artistMatch", "comment"],
};


export const evaluateGuessWithAI = async (guess: string, correctTitle: string, correctArtist: string): Promise<AIGuessResponse> => {
    const prompt = `
        You are a music trivia game judge.
        The user heard a song preview and made a guess.
        Your task is to determine if their guess correctly identifies the song title and/or the artist. Be flexible with spelling errors, partial matches, or descriptive guesses (e.g., "the clocks song by coldplay").

        Correct Song Title: "${correctTitle}"
        Correct Artist: "${correctArtist}"
        User's Guess: "${guess}"

        Evaluate the guess and respond with a JSON object that follows the specified schema.
    `;
    
    try {
        const response = await ai.models.generateContent({
            model: "gemini-2.5-flash",
            contents: prompt,
            config: {
                responseMimeType: "application/json",
                responseSchema: responseSchema,
            },
        });

        const jsonText = response.text.trim();
        const parsedResponse = JSON.parse(jsonText);

        // Basic validation to ensure the parsed object matches the expected structure
        if (typeof parsedResponse.titleMatch === 'boolean' && typeof parsedResponse.artistMatch === 'boolean' && typeof parsedResponse.comment === 'string') {
            return parsedResponse as AIGuessResponse;
        } else {
             throw new Error("AI response did not match the required schema.");
        }

    } catch (error) {
        console.error("Gemini API call failed:", error);
        throw new Error("Failed to get a response from the AI judge.");
    }
};
*/
