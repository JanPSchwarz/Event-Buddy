import { useNavigate } from "react-router";
import { Button } from "@/components/ui/button.tsx";

export default function PageNotFound() {
    const navigate = useNavigate();

    return (
        <div className={ "flex flex-col gap-4 justify-center w-full items-center" }>
            <div className={ "flex flex-col justify-center items-center bg-muted rounded-lg w-1/2" }>
                <img src={ "/404pageNotFound.svg" } alt="Page Not Found"/>
            </div>
            <Button onClick={ () => navigate( -1 ) }>GoBack</Button>
        </div>
    )
}
