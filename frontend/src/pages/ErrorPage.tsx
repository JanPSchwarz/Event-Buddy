import { useRouteError } from "react-router";
import { AxiosError } from "axios";

export default function ErrorPage() {
    const error = useRouteError();


    if ( error instanceof AxiosError ) {
        return <p>
            { error.message }
        </p>
    }

    return (
        <p>Unknown Error</p>
    )
}
