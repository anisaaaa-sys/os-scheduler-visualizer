# OS Scheduler Visualizer

An educational web app that simulates and visualizes classic operating system algorithms. No login, no persistence; all state is in-memory per session. Simulation logic lives in plain Java classes separate from the UI and is unit tested.

## Use case 1: CPU scheduling simulation
- User edits a table of processes (ID, arrival time, burst time, priority). Can add, remove and edit rows inline. A "Load example" button fills in a classic textbook dataset (5 processes).
- User selects an algorithm: FCFS, SJF (non-preemptive), SRTF (preemptive), Round Robin (configurable time quantum), Priority (non-preemptive).
- On "Run", show a Gantt chart as a horizontal timeline of colored blocks labeled with process ID, with time markers below. Idle time is shown as a gray block.
- Below the chart, show a results table: per-process completion, turnaround and waiting time, plus averages.

## Use case 2: Compare algorithms
- Run all algorithms on the current process set and show a summary table of average waiting time, average turnaround time and number of context switches, highlighting the best value in each column.

## Use case 3: Page replacement simulation
- User enters a reference string (e.g. "7 0 1 2 0 3 0 4 2 3 0 3 2") and a number of frames (1-7), and selects FIFO, LRU or Optimal.
- Show a step-by-step grid: one column per reference, rows for frame contents, with page faults highlighted in red and hits in green.
- Show total page faults and hit ratio. A "Compare" button shows the fault counts of all three algorithms side by side.

## Use case 4: Step-through playback (CPU scheduling)
- After "Run", playback controls appear above the Gantt chart: Play/Pause, Step back, Step forward, Reset, and a speed selector (0.5x, 1x, 2x).
- A vertical time cursor moves across the Gantt chart; blocks to the right of the cursor are dimmed.
- Next to the chart, show the current state at time t: current time, running process (with remaining burst), ready queue in order (with remaining burst for each), and completed processes.
- Highlight events at the current tick: arrivals, preemptions, completions (e.g. a short label like "t=1: P2 arrives, preempts P1").
- The schedulers produce a per-tick trace (time, running process, ready queue contents in order, events) as part of SchedulingResult. The UI must read this trace and must not reconstruct queue state from Gantt slices.

## UI
Two main views in a side navigation ("CPU Scheduling", "Page Replacement"). Lumo theme with a dark-mode toggle. Gantt chart and frame grid built from styled components, with no commercial add-ons.
