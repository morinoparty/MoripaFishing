import { v4 as uuidv4, v7 as uuidv7 } from "uuid";

export const UUIDV7 = () => {
    return <>{uuidv7()}</>;
};

export const UUIDV4 = () => {
    return <>{uuidv4()}</>;
};
