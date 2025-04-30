export const TimeStamp = () => {
    return <>{toISOStringWithTimezone(new Date())}</>;
};

function toISOStringWithTimezone(date: Date): string {
    const pad = (str: string): string => `0${str}`.slice(-2);
    const year = date.getFullYear().toString();
    const month = pad((date.getMonth() + 1).toString());
    const day = pad(date.getDate().toString());
    const hour = pad(date.getHours().toString());
    const min = pad(date.getMinutes().toString());
    const sec = pad(date.getSeconds().toString());
    const tz = -date.getTimezoneOffset();
    const sign = tz >= 0 ? "+" : "-";
    const tzHour = pad((tz / 60).toString());
    const tzMin = pad((tz % 60).toString());

    return `${year}-${month}-${day}T${hour}:${min}:${sec}${sign}${tzHour}:${tzMin}`;
}
