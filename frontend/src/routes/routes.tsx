import ErrorPage from "@/pages/ErrorPage";
import { createBrowserRouter } from "react-router";
import RootLayout from "@/layouts/RootLayout.tsx";
import PageNotFound from "@/pages/PageNotFound.tsx";
import SettingsPage from "@/pages/SettingsPage.tsx";

export const routes = createBrowserRouter( [
    {
        path: "/",
        element: <RootLayout/>,
        errorElement: <ErrorPage/>,
        children: [
            // Define child routes
            { path: "profile", element: <div>Profile Page</div> },
            { path: "settings/:userId", element: <SettingsPage/> },
            { path: "*", element: <PageNotFound/> }
        ],
    }
] );
