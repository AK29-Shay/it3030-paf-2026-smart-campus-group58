const API_BASE = process.env.API_BASE || "http://localhost:8080";
const PASSWORD = "ChangeMe123!";

const accounts = {
  admin: { name: "Group 58 Admin", email: "admin@example.com", password: PASSWORD, role: "ADMIN" },
  student: { name: "Group 58 Student", email: "student@example.com", password: PASSWORD, role: "USER" },
  technician: {
    name: "Group 58 Technician",
    email: "technician@example.com",
    password: PASSWORD,
    role: "TECHNICIAN",
    technicianSpecialty: "GENERAL",
  },
  nuwan: { name: "Nuwan Fernando", email: "nuwan.fernando@example.com", password: PASSWORD, role: "USER" },
  ishara: { name: "Ishara Perera", email: "ishara.perera@example.com", password: PASSWORD, role: "USER" },
  malith: {
    name: "Malith Network Support",
    email: "malith.network@example.com",
    password: PASSWORD,
    role: "TECHNICIAN",
    technicianSpecialty: "NETWORK",
  },
};

const resources = [
  {
    name: "Innovation Hub",
    type: "FACILITY",
    category: "MEETING_ROOM",
    capacity: 28,
    location: "New Academic Block - Level 4",
    availabilityStart: "08:00:00",
    availabilityEnd: "19:00:00",
    description: "Flexible collaboration studio for project demos, viva practice, and sponsor reviews.",
    status: "ACTIVE",
  },
  {
    name: "Robotics Lab",
    type: "FACILITY",
    category: "LAB",
    capacity: 36,
    location: "Engineering Complex - Level 2",
    availabilityStart: "08:00:00",
    availabilityEnd: "18:00:00",
    description: "Specialized lab with benches, testing kits, and supervised equipment access.",
    status: "ACTIVE",
  },
  {
    name: "Media Studio Camera Kit",
    type: "EQUIPMENT",
    category: "CAMERA",
    capacity: 1,
    location: "Media Unit Store",
    availabilityStart: "09:00:00",
    availabilityEnd: "17:00:00",
    description: "Camera, tripod, and microphone bundle for academic content recording.",
    status: "ACTIVE",
  },
  {
    name: "Engineering Seminar Hall",
    type: "FACILITY",
    category: "LECTURE_HALL",
    capacity: 90,
    location: "Engineering Complex - Ground Floor",
    availabilityStart: "08:00:00",
    availabilityEnd: "18:00:00",
    description: "Temporarily unavailable due to projector and lighting maintenance.",
    status: "OUT_OF_SERVICE",
  },
];

function jsonHeaders(token) {
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, options);
  const payload = await parseResponse(response);

  if (!response.ok) {
    const message = typeof payload === "string" ? payload : payload?.message || JSON.stringify(payload);
    const error = new Error(`${options.method || "GET"} ${path} failed (${response.status}): ${message}`);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

async function login(account) {
  const payload = await api("/api/auth/login", {
    method: "POST",
    headers: jsonHeaders(),
    body: JSON.stringify({
      email: account.email,
      password: account.password,
      role: account.role,
    }),
  });

  return payload;
}

async function signupOrLogin(account) {
  try {
    const payload = await api("/api/auth/signup", {
      method: "POST",
      headers: jsonHeaders(),
      body: JSON.stringify(account),
    });
    console.log(`Created signup: ${account.email}`);
    return payload;
  } catch (error) {
    if (error.status !== 400 && error.status !== 409 && error.status !== 500) {
      throw error;
    }
    const payload = await login(account);
    console.log(`Using existing signup: ${account.email}`);
    return payload;
  }
}

async function ensureResource(resource, adminToken) {
  const allResources = await api("/api/resources", {
    headers: jsonHeaders(adminToken),
  });
  const existing = allResources.find((item) => item.name.toLowerCase() === resource.name.toLowerCase());

  if (!existing) {
    const created = await api("/api/resources", {
      method: "POST",
      headers: jsonHeaders(adminToken),
      body: JSON.stringify(resource),
    });
    console.log(`Created resource: ${resource.name}`);
    return created;
  }

  const updated = await api(`/api/resources/${existing.id}`, {
    method: "PUT",
    headers: jsonHeaders(adminToken),
    body: JSON.stringify({ ...existing, ...resource, id: existing.id }),
  });
  console.log(`Updated resource: ${resource.name}`);
  return updated;
}

function futureSlot(index, durationMinutes = 60) {
  const now = new Date();
  const start = new Date(now.getTime() + (75 + index * 90) * 60 * 1000);

  if (start.getHours() >= 18 || start.getHours() < 8) {
    start.setDate(start.getDate() + 1);
    start.setHours(9 + index, 0, 0, 0);
  }

  start.setMinutes(Math.ceil(start.getMinutes() / 15) * 15, 0, 0);
  const end = new Date(start.getTime() + durationMinutes * 60 * 1000);
  return {
    startTime: toLocalDateTime(start),
    endTime: toLocalDateTime(end),
  };
}

function toLocalDateTime(date) {
  const pad = (value) => String(value).padStart(2, "0");
  return [
    date.getFullYear(),
    "-",
    pad(date.getMonth() + 1),
    "-",
    pad(date.getDate()),
    "T",
    pad(date.getHours()),
    ":",
    pad(date.getMinutes()),
    ":",
    pad(date.getSeconds()),
  ].join("");
}

async function ensureBooking({ userToken, adminToken, resourceName, purpose, attendees, slotIndex, finalStatus }) {
  const existingBookings = await api("/api/bookings", {
    headers: jsonHeaders(adminToken),
  });
  let booking = existingBookings.find((item) => item.purpose === purpose);

  if (!booking) {
    booking = await api("/api/bookings", {
      method: "POST",
      headers: jsonHeaders(userToken),
      body: JSON.stringify({
        resourceName,
        purpose,
        attendees,
        ...futureSlot(slotIndex),
      }),
    });
    console.log(`Created booking: ${purpose}`);
  } else {
    console.log(`Using existing booking: ${purpose}`);
  }

  if (finalStatus === "APPROVED" && booking.status === "PENDING") {
    booking = await api(`/api/bookings/${booking.id}/approve`, {
      method: "PUT",
      headers: jsonHeaders(adminToken),
    });
    console.log(`Approved booking: ${purpose}`);
  }

  if (finalStatus === "REJECTED" && booking.status === "PENDING") {
    booking = await api(`/api/bookings/${booking.id}/reject`, {
      method: "PUT",
      headers: jsonHeaders(adminToken),
      body: JSON.stringify({
        reason: "Slot reserved for urgent faculty event setup.",
      }),
    });
    console.log(`Rejected booking: ${purpose}`);
  }

  if (finalStatus === "CHECKED_IN" && booking.status === "PENDING") {
    booking = await api(`/api/bookings/${booking.id}/approve`, {
      method: "PUT",
      headers: jsonHeaders(adminToken),
    });
  }

  if (finalStatus === "CHECKED_IN" && booking.status === "APPROVED") {
    booking = await api(`/api/bookings/checkin/${booking.id}`, {
      method: "PUT",
      headers: jsonHeaders(userToken),
    });
    console.log(`Checked in booking: ${purpose}`);
  }

  return booking;
}

async function ensureTicket({ userToken, adminToken, technicianId, createdBy, title, description, category, priority, finalStatus }) {
  const existingTickets = await api("/api/tickets", {
    headers: jsonHeaders(adminToken),
  });
  let ticket = existingTickets.find((item) => item.title === title);

  if (!ticket) {
    const form = new FormData();
    form.append("title", title);
    form.append("description", description);
    form.append("category", category);
    form.append("priority", priority);
    form.append("createdBy", String(createdBy));

    ticket = await api("/api/tickets", {
      method: "POST",
      headers: { Authorization: `Bearer ${userToken}` },
      body: form,
    });
    console.log(`Created ticket: ${title}`);
  } else {
    console.log(`Using existing ticket: ${title}`);
  }

  if (technicianId && !ticket.assignedTo) {
    ticket = await api(`/api/tickets/${ticket.id}/assign`, {
      method: "PUT",
      headers: jsonHeaders(adminToken),
      body: JSON.stringify({
        assignedTo: technicianId,
        reason: "Demo evidence assignment for the final report workflow.",
      }),
    });
    console.log(`Assigned ticket: ${title}`);
  }

  if (finalStatus && ticket.status !== finalStatus) {
    ticket = await api(`/api/tickets/${ticket.id}/status`, {
      method: "PUT",
      headers: jsonHeaders(adminToken),
      body: JSON.stringify({ status: finalStatus }),
    });
    console.log(`Updated ticket status: ${title} -> ${finalStatus}`);
  }

  return ticket;
}

async function main() {
  console.log(`Seeding Smart Campus demo data at ${API_BASE}`);

  const admin = await signupOrLogin(accounts.admin);
  const student = await signupOrLogin(accounts.student);
  const technician = await signupOrLogin(accounts.technician);
  const nuwan = await signupOrLogin(accounts.nuwan);
  const ishara = await signupOrLogin(accounts.ishara);
  const malith = await signupOrLogin(accounts.malith);

  const adminToken = admin.token;

  for (const resource of resources) {
    await ensureResource(resource, adminToken);
  }

  await ensureBooking({
    userToken: student.token,
    adminToken,
    resourceName: "Innovation Hub",
    purpose: "PAF group 58 command center viva rehearsal",
    attendees: 18,
    slotIndex: 0,
    finalStatus: "PENDING",
  });

  await ensureBooking({
    userToken: nuwan.token,
    adminToken,
    resourceName: "Robotics Lab",
    purpose: "Robotics club sensor calibration workshop",
    attendees: 24,
    slotIndex: 1,
    finalStatus: "APPROVED",
  });

  await ensureBooking({
    userToken: ishara.token,
    adminToken,
    resourceName: "Media Studio Camera Kit",
    purpose: "Student council orientation video recording",
    attendees: 4,
    slotIndex: 2,
    finalStatus: "CHECKED_IN",
  });

  await ensureBooking({
    userToken: student.token,
    adminToken,
    resourceName: "Computer Lab 3",
    purpose: "Data structures revision lab booking",
    attendees: 32,
    slotIndex: 3,
    finalStatus: "REJECTED",
  });

  await ensureTicket({
    userToken: student.token,
    adminToken,
    technicianId: technician.user.id,
    createdBy: student.user.id,
    title: "Projector flickering in Computer Lab 3",
    description: "The projector flickers every few minutes during lectures and needs a cable or bulb inspection.",
    category: "EQUIPMENT",
    priority: "HIGH",
    finalStatus: "IN_PROGRESS",
  });

  await ensureTicket({
    userToken: nuwan.token,
    adminToken,
    technicianId: malith.user.id,
    createdBy: nuwan.user.id,
    title: "Weak Wi-Fi near Innovation Hub",
    description: "Teams lose connectivity during demo rehearsals near the rear tables.",
    category: "NETWORK",
    priority: "MEDIUM",
    finalStatus: "OPEN",
  });

  await ensureTicket({
    userToken: ishara.token,
    adminToken,
    technicianId: technician.user.id,
    createdBy: ishara.user.id,
    title: "Air conditioning noise in Meeting Room B",
    description: "The indoor unit makes a rattling sound and affects group discussions.",
    category: "ELECTRICAL",
    priority: "LOW",
    finalStatus: "RESOLVED",
  });

  const commandCenter = await api("/api/admin/command-center", {
    headers: jsonHeaders(adminToken),
  });

  console.log("Demo data ready.");
  console.log(
    JSON.stringify(
      {
        users: commandCenter.metrics.totalUsers,
        resources: commandCenter.metrics.totalResources,
        bookings: commandCenter.metrics.totalBookings,
        tickets: commandCenter.metrics.totalTickets,
        pendingBookings: commandCenter.metrics.pendingBookings,
        outOfServiceResources: commandCenter.metrics.outOfServiceResources,
      },
      null,
      2,
    ),
  );
}

main().catch((error) => {
  console.error(error.message);
  process.exitCode = 1;
});
