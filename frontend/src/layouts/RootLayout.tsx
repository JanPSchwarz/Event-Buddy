import { Outlet, useLocation } from "react-router";
import HomePage from "@/pages/HomePage.tsx";

export default function RootLayout() {
    const location = useLocation();
    const isHomePage = location.pathname === '/';


    return (
        <div className={ "flex-1" }>
            { isHomePage && <HomePage/> }
            <Outlet/>
        </div>
    )
}