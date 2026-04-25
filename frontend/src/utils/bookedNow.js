import API from "../services/api";
import dayjs from "./dayjs";

export async function getCurrentlyBookedResources() {
  try {
    const response = await API.get("/bookings");
    const bookings = Array.isArray(response.data) ? response.data : [];
    const now = dayjs();

    return new Set(
      bookings
        .filter((booking) => {
          const start = dayjs(booking.startTime);
          const end = dayjs(booking.endTime);
          return now.isAfter(start) && now.isBefore(end);
        })
        .map((booking) => booking.resourceId ?? booking.resource?.id)
        .filter(Boolean)
    );
  } catch {
    return new Set();
  }
}
