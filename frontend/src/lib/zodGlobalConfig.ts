import { z } from "zod";

z.config( {
    customError: ( iss ) => {
        switch ( iss.code ) {
            case "too_small":
                if ( iss.minimum == 1 ) {
                    return "This field is required";
                } else return "At least " + iss.minimum + " characters";
            case "too_big":
                return "Not more than " + iss.maximum + " characters";
            default:
                return "Invalid value";

        }
    }
} )