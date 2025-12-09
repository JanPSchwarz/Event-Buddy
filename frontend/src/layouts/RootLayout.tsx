import { Outlet, useLocation } from "react-router";
import HomePage from "@/pages/HomePage.tsx";
import Header from "@/components/Header.tsx";

export default function RootLayout() {
    const location = useLocation();
    const isHomePage = location.pathname === '/';


    return (

        <>
            <Header/>
            <div className={ "flex-1 mt-4 flex items-center justify-center" }>
                { isHomePage && <HomePage/> }
                <Outlet/>
            </div>
        </>
    )
}