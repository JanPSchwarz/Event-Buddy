import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ThemeProvider } from "@/context/ThemeProvider.tsx";
import { UserProvider } from "@/context/UserProvider.tsx";
import { Toaster } from "sonner";

const queryClient = new QueryClient( {
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5, // 5 minutes
            retry: 1,
        },
    },
} )

createRoot( document.getElementById( 'root' )! ).render(
    <StrictMode>
        <ThemeProvider defaultTheme={ "dark" } storageKey={ "vite-ui-theme" }>
            <QueryClientProvider client={ queryClient }>
                <UserProvider>
                    <App/>
                    <Toaster richColors={ true }/>
                </UserProvider>
            </QueryClientProvider>
        </ThemeProvider>
    </StrictMode>,
)
