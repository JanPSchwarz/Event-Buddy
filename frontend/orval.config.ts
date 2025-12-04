import { defineConfig } from "orval";

const isDev = process.env.NODE_ENV !== "production";

const baseURL = isDev
    ? "http://localhost:8080"
    : "https://teamproject-wrms.onrender.com/";
export default defineConfig( {
    eventBuddyApi: {
        input: {
            target: "./openapi.json",
        },
        output: {
            mode: "tags-split",
            target: "src/api/generated",
            client: "react-query",
            baseUrl: baseURL,
        },
    },
} );
