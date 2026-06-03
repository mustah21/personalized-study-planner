import { useState, useEffect } from "react";
import {
  startOfMonth,
  endOfMonth,
  eachDayOfInterval,
  format,
  addMonths,
  subMonths,
  isSameDay,
} from "date-fns";

import PageHeader from "../ui/PageHeader";
import Button from "../ui/Button";
import Card from "../ui/Card";
import Input from "../ui/Input";
import Select from "../ui/Select";

import type { CalendarEvent, EventType } from "../types";
import { useTranslation } from "react-i18next";

export default function CalendarPage() {
  const { t, i18n } = useTranslation();

  const currentLang = (i18n.resolvedLanguage || i18n.language || "en")
    .toLowerCase()
    .split("-")[0];

  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(
    format(new Date(), "yyyy-MM-dd")
  );

  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [showAddEvent, setShowAddEvent] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [newType, setNewType] = useState<EventType>("Class");

  const days = eachDayOfInterval({
    start: startOfMonth(currentMonth),
    end: endOfMonth(currentMonth),
  });

  const selectedEvents = events.filter((e) => e.date === selectedDate);

  const monthNames = {
    en: [
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December",
    ],
    vi: [
      "tháng 1",
      "tháng 2",
      "tháng 3",
      "tháng 4",
      "tháng 5",
      "tháng 6",
      "tháng 7",
      "tháng 8",
      "tháng 9",
      "tháng 10",
      "tháng 11",
      "tháng 12",
    ],
    fi: [
      "tammikuu",
      "helmikuu",
      "maaliskuu",
      "huhtikuu",
      "toukokuu",
      "kesäkuu",
      "heinäkuu",
      "elokuu",
      "syyskuu",
      "lokakuu",
      "marraskuu",
      "joulukuu",
    ],
    ne: [
      "जनवरी",
      "फेब्रुअरी",
      "मार्च",
      "अप्रिल",
      "मे",
      "जुन",
      "जुलाई",
      "अगस्ट",
      "सेप्टेम्बर",
      "अक्टोबर",
      "नोभेम्बर",
      "डिसेम्बर",
    ],
  };

  const getMonthName = (date: Date) => {
    const names =
      monthNames[currentLang as keyof typeof monthNames] || monthNames.en;
    return names[date.getMonth()];
  };

  const formatMonthYear = (date: Date) => {
    const month = getMonthName(date);
    const year = date.getFullYear();

    if (currentLang === "vi") {
      return `${month} năm ${year}`;
    }

    if (currentLang === "fi") {
      return `${month} ${year}`;
    }

    if (currentLang === "ne") {
      return `${month} ${year}`;
    }

    return `${month} ${year}`;
  };

  const formatFullDate = (date: Date) => {
    const day = date.getDate();
    const month = getMonthName(date);
    const year = date.getFullYear();

    if (currentLang === "vi") {
      return `${day} ${month}, ${year}`;
    }

    if (currentLang === "fi") {
      return `${month} ${day}, ${year}`;
    }

    if (currentLang === "ne") {
      return `${month} ${day}, ${year}`;
    }

    return `${month} ${day}, ${year}`;
  };

  const monthYearText = formatMonthYear(currentMonth);
  const selectedDateText = formatFullDate(new Date(selectedDate));

  useEffect(() => {
    const start = startOfMonth(currentMonth);
    const end = endOfMonth(currentMonth);
    const selected = new Date(selectedDate);

    if (selected < start || selected > end) {
      setSelectedDate(format(start, "yyyy-MM-dd"));
    }
  }, [currentMonth, selectedDate]);

  const handleAddEvent = () => {
    if (!newTitle.trim()) return;

    const newEvent: CalendarEvent = {
      id: crypto.randomUUID(),
      title: newTitle.trim(),
      type: newType,
      date: selectedDate,
    };

    setEvents((prev) => [...prev, newEvent]);
    setNewTitle("");
    setNewType("Class");
    setShowAddEvent(false);
  };

  const handleICSImport = async (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const text = await file.text();
    const lines = text.split("\n");

    const imported: CalendarEvent[] = [];
    let title = "";
    let date = "";

    for (const line of lines) {
      if (line.startsWith("SUMMARY:")) {
        title = line.replace("SUMMARY:", "").trim();
      }
      if (line.startsWith("DTSTART")) {
        const raw = line.split(":")[1].trim();
        date = `${raw.slice(0, 4)}-${raw.slice(4, 6)}-${raw.slice(6, 8)}`;
      }
      if (line.startsWith("END:VEVENT")) {
        imported.push({
          id: crypto.randomUUID(),
          title,
          type: "Other",
          date,
        });
      }
    }

    setEvents((prev) => [...prev, ...imported]);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={t("calendar.title")}
        subtitle={t("calendar.subtitle")}
      >
        <div className="flex gap-3">
          <input
            type="file"
            accept=".ics"
            className="hidden"
            id="icsUpload"
            onChange={handleICSImport}
          />

          <Button
            className="bg-purple-500 hover:bg-purple-600 text-white"
            onClick={() => setShowAddEvent(true)}
          >
            {t("addevent.title")}
          </Button>
        </div>
      </PageHeader>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 rounded-3xl border border-purple-100 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <Button
              variant="outline"
              className="border-purple-300 text-purple-600 hover:bg-purple-100"
              onClick={() => setCurrentMonth((prev) => subMonths(prev, 1))}
            >
              {t("calendar.previous")}
            </Button>

            <h3 className="text-xl font-semibold text-purple-700">
              {monthYearText}
            </h3>

            <Button
              variant="outline"
              className="border-purple-300 text-purple-600 hover:bg-purple-100"
              onClick={() => setCurrentMonth((prev) => addMonths(prev, 1))}
            >
              {t("calendar.next")}
            </Button>
          </div>

          <div className="grid grid-cols-7 gap-3">
            {days.map((day) => {
              const iso = format(day, "yyyy-MM-dd");
              const isSelected = iso === selectedDate;
              const isToday = isSameDay(day, new Date());

              return (
                <button
                  key={iso}
                  onClick={() => setSelectedDate(iso)}
                  className={`
                    p-3 rounded-xl text-center transition shadow-sm
                    ${
                      isSelected
                        ? "bg-purple-500 text-white shadow-md"
                        : "bg-purple-50 hover:bg-purple-100"
                    }
                    ${
                      !isSelected && isToday
                        ? "border-2 border-purple-400"
                        : ""
                    }
                  `}
                >
                  {format(day, "d")}
                </button>
              );
            })}
          </div>
        </Card>

        <Card className="rounded-3xl border border-purple-100 shadow-sm">
          <h3 className="text-lg font-semibold text-purple-700">
            {selectedDateText}
          </h3>
          <p className="text-gray-500">
            {t("calendar.scheduledItems", { count: selectedEvents.length })}
          </p>

          {selectedEvents.length === 0 ? (
            <p className="text-gray-500 mt-3">{t("calendar.noEvents")}</p>
          ) : (
            <ul className="mt-4 space-y-3">
              {selectedEvents.map((e) => (
                <li
                  key={e.id}
                  className="p-3 bg-purple-50 rounded-xl border border-purple-100"
                >
                  <strong className="text-purple-700">{e.title}</strong>
                  <p className="text-sm text-gray-500">
                    {e.type === "Class" && t("eventtype.class")}
                    {e.type === "Exam" && t("eventtype.exam")}
                    {e.type === "Assignment" && t("eventtype.assignment")}
                    {e.type === "Other" && t("eventtype.other")}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>

      {showAddEvent && (
        <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
          <Card className="w-full max-w-md rounded-3xl border border-purple-100 shadow-xl p-6">
            <h3 className="text-xl font-semibold text-purple-700 mb-4">
              {t("addevent.title")} - {selectedDateText}
            </h3>

            <label className="block text-sm font-medium text-gray-600 mb-1">
              {t("addevent.name")}
            </label>
            <Input
              placeholder={t("addevent.placeholderName")}
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              className="mb-4 bg-purple-50/40 border-purple-200 focus:border-purple-400"
            />

            <label className="block text-sm font-medium text-gray-600 mb-1">
              {t("addevent.type")}
            </label>
            <Select
              value={newType}
              onChange={(e) => setNewType(e.target.value as EventType)}
              className="mb-4 bg-purple-50/40 border-purple-200 focus:border-purple-400"
            >
              <option value="Class">{t("eventtype.class")}</option>
              <option value="Exam">{t("eventtype.exam")}</option>
              <option value="Assignment">{t("eventtype.assignment")}</option>
              <option value="Other">{t("eventtype.other")}</option>
            </Select>

            <div className="flex justify-end gap-3 mt-6">
              <Button
                variant="outline"
                className="border-purple-300 text-purple-600 hover:bg-purple-100"
                onClick={() => setShowAddEvent(false)}
              >
                {t("addevent.cancelButton")}
              </Button>

              <Button
                className="bg-purple-500 hover:bg-purple-600 text-white"
                onClick={handleAddEvent}
              >
                {t("addevent.saveButton")}
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}