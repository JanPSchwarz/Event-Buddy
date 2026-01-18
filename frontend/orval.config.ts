import { defineConfig } from "orval";

const isDev = process.env.NODE_ENV !== "production";

const baseURL = isDev
    ? "http://localhost:8080"
    : "";
export default defineConfig( {
    eventBuddyApi: {
        input: {
            target: "./openapi.json",
        }
        ,
        output: {
            mode: "tags-split",
            target:
                "src/api/generated",
            client:
                "react-query",
            baseUrl: baseURL,
            override:
                {
                    query: {
                        version: 5
                    }
                }
        }
    },
    zodClient: {
        input: {
            target: "./openapi.json",
        }
        ,
        output: {
            mode: "tags-split",
            target:
                "src/api/generated",
            client:
                "zod",
            baseUrl: baseURL,
            fileExtension: ".zod.ts",
            override:
                {
                    query: {
                        version: 5
                    }
                }
        }
        ,
    }
} );
