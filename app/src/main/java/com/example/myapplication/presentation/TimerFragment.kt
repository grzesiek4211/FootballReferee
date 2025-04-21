package com.example.myapplication.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.R


class TimerFragment : CurrentTimeFragment(R.layout.timer_fragment, R.id.current_time) {

    companion object {
        fun newInstance(timerDuration: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putLong("TIMER_DURATION", timerDuration)
            }
        }
        private const val TIMER_REQUEST_CODE = 100 // Unikalny kod dla PendingIntent AlarmManagera
        private const val STATE_TIME_REMAINING = "state_time_remaining"
        private const val STATE_IS_PAUSED = "state_is_paused"
        private const val TAG = "TimerFragment" // Tag dla logów
    }

    private lateinit var timerTextView: TextView
    private lateinit var buttonPausePlay: Button
    private lateinit var buttonRestart: Button
    private var timer: CountDownTimer? = null // Zmień na nullable
    private var isPaused = false
    private var initialTimerValue: Long = DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L
    private var timeRemaining: Long = initialTimerValue


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")

        // Przywrócenie stanu PRZED nadpisaniem argumentami, jeśli istnieje
        savedInstanceState?.let {
            timeRemaining = it.getLong(STATE_TIME_REMAINING, initialTimerValue)
            isPaused = it.getBoolean(STATE_IS_PAUSED, false)
            Log.d(TAG, "onCreateView: Restored state timeRemaining=${timeRemaining/1000}s, isPaused=${isPaused}")
        }

        // Pobranie argumentów tylko jeśli nie przywrócono stanu
        if (savedInstanceState == null) {
            arguments?.let {
                timeRemaining = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L)
                initialTimerValue = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L)
                Log.d(TAG, "onCreateView: Initial timer duration from arguments: ${initialTimerValue / 1000}s")
            }
        } else {
            // Jeśli przywrócono stan, zaktualizuj initialTimerValue na wypadek, gdyby argumenty były inne
            // To może być skomplikowane - zazwyczaj przy odtwarzaniu stanu ignorejemy argumenty.
            // Jeśli chcesz, żeby restart zawsze wracał do wartości z argumentów,
            // możesz zapisać initialTimerValue też w stanie.
            // Na potrzeby tej wersji zakładamy, że initialTimerValue się nie zmienia dynamicznie
        }

        val view = super.onCreateView(inflater, container, savedInstanceState)

        timerTextView = view.findViewById(R.id.timerTextView)
        buttonPausePlay = view.findViewById(R.id.button_pause_play)
        buttonRestart = view.findViewById(R.id.button_restart)

        initializeTimerDisplay() // Ustaw początkowe wyświetlanie

        // Logika uruchamiania/wznawiania timera UI i AlarmManager przy tworzeniu/odświeżaniu widoku
        if (!isPaused) {
            // Jeśli timer nie był pauzowany, sprawdź stan AlarmManager i wystartuj odpowiednio
            val alarmIsScheduled = getAlarmPendingIntentExists()
            Log.d(TAG, "onCreateView: Timer was not paused. Alarm PendingIntent exists: $alarmIsScheduled")

            if (alarmIsScheduled) {
                // Alarm jest już zaplanowany. Uruchom tylko timer UI, który będzie odliczał.
                // Czas pozostały został przywrócony ze stanu.
                startUITimer(timeRemaining)
                Log.d(TAG, "onCreateView: System alarm scheduled, starting UI timer only.")
            } else {
                // Alarm nie jest zaplanowany (np. pierwsze uruchomienie, po reboocie bez obsługi, proces zabity bez zapisu stanu).
                // Uruchom timer od początku lub od przywróconego czasu i zaplanuj alarm.
                Log.d(TAG, "onCreateView: System alarm NOT scheduled, starting full timer sequence.")
                // startTimer(timeRemaining) // Ta metoda uruchamia UI timer I planuje AlarmManagera
                // Może lepiej:
                startUITimer(timeRemaining) // Uruchom UI timer
                scheduleAlarm(timeRemaining) // Zaplanuj AlarmManager
            }
            buttonPausePlay.text = "⏸"
        } else {
            // Jeśli timer był pauzowany, tylko ustaw odpowiedni tekst na przycisku i wyświetlanie
            initializeTimerDisplay() // Upewnij się, że wyświetla poprawny, zapisany czas
            buttonPausePlay.text = "▶️"
            Log.d(TAG, "onCreateView: Timer was paused.")
            // Upewnij się, że systemowy alarm NIE jest zaplanowany, jeśli był pauzowany
            if (getAlarmPendingIntentExists()) {
                Log.w(TAG, "onCreateView: Timer was paused but system alarm still exists! Cancelling.")
                cancelAlarm() // Anuluj, bo nie powinien działać w tle gdy pauza
            }
        }


        buttonPausePlay.setOnClickListener {
            if (isPaused) {
                Log.d(TAG, "buttonPausePlay clicked: Resuming timer.")
                startTimer(timeRemaining) // Startuje timer UI i planuje AlarmManager
                buttonPausePlay.text = "⏸"
                isPaused = false
            } else {
                Log.d(TAG, "buttonPausePlay clicked: Pausing timer. Time remaining: ${timeRemaining / 1000}s")
                pauseTimer() // Pauzuje timer UI i anuluje AlarmManager
                buttonPausePlay.text = "▶️"
                isPaused = true
            }
            // Nie stopujemy serwisu alarmowego stąd. Zostanie zatrzymany przez FullScreenAlarmActivity
            // lub przy anulowaniu/restarcie jeśli działał w tle i nie został zatrzymany przez użytkownika.
        }

        buttonRestart.setOnClickListener {
            Log.d(TAG, "buttonRestart clicked: Restarting timer.")
            cancelTimer() // Anuluj timer UI
            cancelAlarm() // Anuluj zaplanowany alarm systemowy
            timeRemaining = initialTimerValue // Resetuj czas do wartości początkowej
            initializeTimerDisplay() // Odśwież wyświetlanie
            startTimer(timeRemaining) // Uruchom nowy timer UI i zaplanuj nowy alarm
            buttonPausePlay.text = "⏸"
            isPaused = false
            // Upewnij się, że serwis alarmowy jest zatrzymany, jeśli działał (np. użytkownik restartuje
            // timer zanim zdąży kliknąć stop na pełnoekranowym alarmie)
            stopAlarmServiceIfRunning()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // Można tu dodać logikę sprawdzającą, czy po powrocie do aplikacji
        // serwis alarmowy (dźwięk/wibracje) nadal działa i ewentualnie go zatrzymać
        // oraz zaktualizować UI (np. pokazać przycisk Stop zamiast Pauza/Play).
        // To wymaga komunikacji zwrotnej z AlarmNotificationService (np. BroadcastReceiverem).
        // Na razie skupiamy się na samym budzeniu.
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        // Gdy Fragment jest pauzowany (np. użytkownik przechodzi do innej aplikacji),
        // timer UI może przestać działać. Systemowy alarm zaplanowany przez AlarmManager
        // przejmie odpowiedzialność za wyzwolenie o czasie. Nie anulujemy AlarmManagera tutaj,
        // chyba że explicitly chcemy, aby timer działał tylko gdy Fragment jest aktywny.
        // Nasz cel to działanie w tle, więc NIE anulujemy AlarmManagera w onPause.
        // cancelTimer() // Opcjonalnie: zatrzymaj UI timer gdy Fragment przestaje być widoczny
    }


    private fun initializeTimerDisplay() {
        val minutes = timeRemaining / 1000 / 60
        val seconds = (timeRemaining / 1000) % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
        Log.d(TAG, "Display initialized to: ${timerTextView.text}")
    }

    // Uruchamia tylko CountDownTimer dla UI (odpowiedzialny za odświeżanie TextView)
    private fun startUITimer(timeMillis: Long) {
        Log.d(TAG, "Starting *only* UI timer for ${timeMillis / 1000}s")
        cancelTimer() // Anuluj poprzedni timer UI, jeśli istnieje

        // Jeśli czas = 0, timer UI już się skończył
        if (timeMillis <= 0) {
            timerTextView.text = "00:00"
            Log.d(TAG, "startUITimer: Initial time <= 0, timer finished instantly.")
            // Tutaj można wywołać logikę 'koniec minutnika' dla UI, np. zmiana wyglądu przycisków
            // ale główny alarm powinien być wywołany przez AlarmManager
            // triggerAlarm() // Nie wywołuj bezpośrednio! AlarmManager to zrobi.
            return
        }


        timer = object : CountDownTimer(timeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                Log.d(TAG, "UI Timer finished.")
                timeRemaining = 0 // Upewnij się, że czas jest 0
                timerTextView.text = "00:00"
                // Timer UI skończył odliczać. Systemowy alarm powinien już być zaplanowany i zadzwonić niezależnie.
                // Tutaj możesz zaktualizować UI tak, aby odzwierciedlało koniec odliczania (np. pokazać przycisk STOP).
                // triggerAlarm() // Nie wywołuj bezpośrednio! AlarmManager to zrobi.
            }
        }.start()
    }

    // Uruchamia timer UI I planuje alarm systemowy
    private fun startTimer(timeMillis: Long) {
        Log.d(TAG, "Starting UI timer and scheduling system alarm for ${timeMillis / 1000}s from now.")
        startUITimer(timeMillis) // Uruchom timer UI
        scheduleAlarm(timeMillis) // Zaplanuj systemowy alarm
    }


    // Pauzuje CountDownTimer UI i anuluje zaplanowany alarm systemowy
    private fun pauseTimer() {
        Log.d(TAG, "Pausing UI timer and cancelling system alarm.")
        cancelTimer() // Zatrzymaj tylko timer UI
        cancelAlarm() // Anuluj zaplanowany alarm systemowy
    }

    // Anuluje CountDownTimer DLA UI
    private fun cancelTimer() {
        timer?.cancel()
        timer = null
        Log.d(TAG, "UI timer cancelled.")
    }

    // --- Metody do obsługi AlarmManager ---

    private fun getAlarmManager(): AlarmManager {
        return requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // Tworzy PendingIntent dla AlarmManager (używany do planowania i anulowania)
    private fun getAlarmPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
            // Opcjonalnie dodaj extra dane, np. ID minutnika
            // putExtra("TIMER_ID", timerId)
        }
        // FLAG_IMMUTABLE jest wymagane od API 31+
        // FLAG_UPDATE_CURRENT jest użyte, aby pobrać istniejący PendingIntent z tym samym kodem i intentem
        return PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE, // Użyj unikalnego, stałego kodu
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Sprawdza, czy NASZ PendingIntent jest zaplanowany w systemie
    private fun getAlarmPendingIntentExists(): Boolean {
        val intent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
        }
        // FLAG_NO_CREATE zwróci null, jeśli PendingIntent z tymi samymi parametrami nie istnieje
        // FLAG_IMMUTABLE jest wymagane od API 31+
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE, // Użyj dokładnie tego samego kodu!
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        val exists = pendingIntent != null
        Log.d(TAG, "Checking if Alarm PendingIntent exists (FLAG_NO_CREATE): $exists")
        return exists
    }


    // Planuje alarm w AlarmManager
    private fun scheduleAlarm(timeUntilFinishMillis: Long) {
        Log.d(TAG, "Scheduling system alarm for ${timeUntilFinishMillis / 1000}s from now.")
        // Anuluj poprzedni alarm, jeśli istnieje, zanim zaplanujesz nowy (np. przy wznowieniu)
        cancelAlarm() // Bezpieczne wywołanie, jeśli alarm nie istnieje

        val alarmManager = getAlarmManager()
        val pendingIntent = getAlarmPendingIntent()

        // Oblicz dokładny czas końca w czasie rzeczywistym (wall clock time)
        val triggerTime = System.currentTimeMillis() + timeUntilFinishMillis

        // Opcjonalny PendingIntent do pokazania systemowego UI nadchodzącego alarmu (wymagany przez setAlarmClock)
        val showIntent = Intent(requireContext(), MainActivity::class.java) // Wskaż Aktywność, którą system ma pokazać
        val showPendingIntent = PendingIntent.getActivity(
            requireContext(),
            TIMER_REQUEST_CODE + 1, // Inny unikalny request code
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)

        // Użyj odpowiedniej metody AlarmManager w zależności od wersji Androida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Dla API 31+ sprawdzamy, czy aplikacja ma zgodę na dokładne alarmy
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d(TAG, "AlarmManager setAlarmClock scheduled (canScheduleExactAlarms granted).")
            } else {
                Log.w(TAG, "Cannot schedule exact alarms. Alarm may be delayed. Using fallback.")
                // Fallback - użyj setExactAndAllowWhileIdle, która obchodzi Doze
                // ALE: może nie być tak punktualna jak setAlarmClock i nie pokazuje systemowego UI alarmu.
                // Na API 31+ powinieneś prosić użytkownika o zgodę na dokładne alarmy, jeśli Ci na tym zależy.
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d(TAG, "AlarmManager setExactAndAllowWhileIdle scheduled (API 31+ fallback).")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Dla API 23-30 (Doze Mode) użyj setExactAndAllowWhileIdle
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d(TAG, "AlarmManager setExactAndAllowWhileIdle scheduled (API 23-30).")
        } else {
            // Dla starszych API użyj setExact (mniej restrykcyjne zarządzanie energią)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d(TAG, "AlarmManager setExact scheduled (<API 23).")
        }
    }

    // Anuluje zaplanowany alarm w AlarmManager
    private fun cancelAlarm() {
        Log.d(TAG, "Attempting to cancel system alarm.")
        val alarmManager = getAlarmManager()
        val pendingIntent = getAlarmPendingIntent() // Pobierz PendingIntent dokładnie tak samo jak przy planowaniu
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "System alarm cancellation requested.")
        // Upewnij się, że service alarmowy jest zatrzymany, jeśli działał
        stopAlarmServiceIfRunning()
    }

    // Metoda pomocnicza do zatrzymania serwisu notyfikacji, jeśli działa
    private fun stopAlarmServiceIfRunning() {
        Log.d(TAG, "Attempting to stop AlarmNotificationService.")
        val alarmServiceIntent = Intent(requireContext(), AlarmNotificationService::class.java)
        try {
            // stopService działa nawet jeśli serwis nie był uruchomiony
            requireContext().stopService(alarmServiceIntent)
            Log.d(TAG, "AlarmNotificationService stop requested.")
        } catch (e: Exception) {
            // Złap wyjątek, np. jeśli serwis nigdy nie był uruchomiony lub już został zatrzymany
            Log.e(TAG, "Error stopping AlarmNotificationService: ${e.message}")
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Zapisz tylko czas pozostały i stan pauzy
        outState.putLong(STATE_TIME_REMAINING, timeRemaining)
        outState.putBoolean(STATE_IS_PAUSED, isPaused)
        Log.d(TAG, "onSaveInstanceState: Saving timeRemaining=${timeRemaining/1000}s, isPaused=${isPaused}")
        // Nie zapisujemy stanu AlarmManager ani timera UI - one zostaną odtworzone/sprawdzone w onCreateView/onViewStateRestored
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored")
        savedInstanceState?.let {
            // Czas pozostały i stan pauzy są już przywrócone w onCreateView
            Log.d(TAG, "onViewStateRestored: Using state restored in onCreateView.")

            // Logika została przeniesiona do onCreateView, aby lepiej obsłużyć różne ścieżki tworzenia Fragmentu
            // po zmianie konfiguracji lub odtworzeniu procesu.
            // Możesz zostawić ten log, ale główna logika przywracania powinna być w onCreateView.
        }
        // Można tutaj opcjonalnie sprawdzić, czy serwis alarmowy działa po powrocie do Fragmentu
        // i zaktualizować UI, np. jeśli użytkownik zatrzymał alarm z pełnoekranowej Aktywności
        // i system powrócił do tego Fragmentu. Wymaga to nasłuchiwania na zatrzymanie serwisu.
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView. Cancelling UI timer.")
        cancelTimer() // Anuluj timer UI - nie jest już potrzebny, gdy widok znika

        // !!! WAŻNE: Zdecyduj, czy minutnik ma działać w tle, gdy Fragment NIE JEST widoczny.
        // Jeśli TAK (typowe dla minutnika/alarmu), NIE anuluj AlarmManager ani nie stopuj serwisu alarmowego tutaj.
        // AlarmManager i AlarmNotificationService powinny działać niezależnie od tego, czy Fragment jest widoczny.
        // Jeśli NIE (minutnik działa tylko na pierwszym planie), to powinieneś anulować AlarmManagera i serwis tutaj.
        // Zakładamy, że MINUTNIK MA DZIAŁAĆ W TLE:
        // cancelAlarm() // <-- NIE WYWOŁUJ, jeśli chcesz, żeby timer dzwonił w tle po wyjściu!
        // stopAlarmServiceIfRunning() // <-- NIE WYWOŁUJ, jeśli chcesz, żeby alarm dzwonił dalej po wyjściu!

        // Jedyny przypadek, gdy możesz chcieć zatrzymać serwis tutaj, to jeśli masz pewność,
        // że Fragment jest niszczony i timer NIE MA dzwonić w tle.
        // Ale dla typowego minutnika, to nie jest pożądane.
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        // Tutaj też nie stopujemy AlarmManagera ani serwisu alarmowego, jeśli mają działać w tle.
    }
}