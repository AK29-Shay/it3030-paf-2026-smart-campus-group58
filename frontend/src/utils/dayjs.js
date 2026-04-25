function dayjs(value = Date.now()) {
  const date = value instanceof Date ? value : new Date(value);

  return {
    valueOf() {
      return date.getTime();
    },
    isAfter(other) {
      return date.getTime() > toDate(other).getTime();
    },
    isBefore(other) {
      return date.getTime() < toDate(other).getTime();
    },
  };
}

function toDate(value) {
  if (value && typeof value === "object" && "valueOf" in value) {
    return new Date(value.valueOf());
  }

  return value instanceof Date ? value : new Date(value);
}

export default dayjs;
