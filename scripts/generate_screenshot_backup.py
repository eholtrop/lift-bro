#!/usr/bin/env python3
"""
Generate screenshot_test_backup.json with realistic test data.
Dates are computed relative to the current date (2 months back).
"""

import json
import os
import sys
from datetime import datetime, timedelta
import random

# ── Categories ──────────────────────────────────────────────────────────
CATEGORIES = [
    {"id": "cat-squat", "name": "Squat", "color": 10821734794166013952},
    {"id": "cat-press", "name": "Press", "color": 10817520509099874304},
    {"id": "cat-deadlift", "name": "Deadlift", "color": 10838745238316408832},
    {"id": "cat-shoulder", "name": "Shoulder", "color": 10825246914698278912},
    {"id": "cat-arm", "name": "Arm", "color": 10832000000000000000},
    {"id": "cat-back", "name": "Back", "color": 10836000000000000000},
]

# ── Variations (3 per category) ─────────────────────────────────────────
VARIATIONS = [
    # Squat
    {"id": "var-back-squat", "lift": "cat-squat", "name": "Back Squat", "reps": 1, "favourite": True, "notes": "Main squat variation", "bodyWeight": False},
    {"id": "var-front-squat", "lift": "cat-squat", "name": "Front Squat", "reps": 1, "favourite": False, "bodyWeight": False},
    {"id": "var-goblet-squat", "lift": "cat-squat", "name": "Goblet Squat", "reps": 1, "favourite": False, "bodyWeight": False},
    # Press
    {"id": "var-bench-press", "lift": "cat-press", "name": "Bench Press", "reps": 1, "favourite": True, "notes": "Flat bench press", "bodyWeight": False},
    {"id": "var-overhead-press", "lift": "cat-press", "name": "Overhead Press", "reps": 1, "favourite": False, "bodyWeight": False},
    {"id": "var-incline-bench", "lift": "cat-press", "name": "Incline Bench Press", "reps": 1, "favourite": False, "bodyWeight": False},
    # Deadlift
    {"id": "var-conventional-deadlift", "lift": "cat-deadlift", "name": "Conventional Deadlift", "reps": 1, "favourite": True, "notes": "Conventional stance", "bodyWeight": False},
    {"id": "var-romanian-deadlift", "lift": "cat-deadlift", "name": "Romanian Deadlift", "reps": 1, "favourite": False, "bodyWeight": False},
    {"id": "var-sumo-deadlift", "lift": "cat-deadlift", "name": "Sumo Deadlift", "reps": 1, "favourite": False, "bodyWeight": False},
    # Shoulder
    {"id": "var-lateral-raise", "lift": "cat-shoulder", "name": "Lateral Raise", "reps": 1, "favourite": True, "bodyWeight": False},
    {"id": "var-face-pull", "lift": "cat-shoulder", "name": "Face Pull", "reps": 1, "favourite": False, "bodyWeight": False},
    {"id": "var-arnold-press", "lift": "cat-shoulder", "name": "Arnold Press", "reps": 1, "favourite": False, "bodyWeight": False},
    # Arm
    {"id": "var-barbell-curl", "lift": "cat-arm", "name": "Barbell Curl", "reps": 1, "favourite": True, "bodyWeight": False},
    {"id": "var-tricep-pushdown", "lift": "cat-arm", "name": "Tricep Pushdown", "reps": 1, "favourite": False, "bodyWeight": False},
    {"id": "var-hammer-curl", "lift": "cat-arm", "name": "Hammer Curl", "reps": 1, "favourite": False, "bodyWeight": False},
    # Back
    {"id": "var-barbell-row", "lift": "cat-back", "name": "Barbell Row", "reps": 1, "favourite": True, "bodyWeight": False},
    {"id": "var-lat-pulldown", "lift": "cat-back", "name": "Lat Pulldown", "reps": 1, "favourite": False, "bodyWeight": False},
    {"id": "var-seated-cable-row", "lift": "cat-back", "name": "Seated Cable Row", "reps": 1, "favourite": False, "bodyWeight": False},
]

# ── Exercise templates per session type ─────────────────────────────────
# Each template: (variation_id, start_weight, end_weight, start_reps, end_reps, tempo)
SESSION_TEMPLATES = {
    "squat_back": [
        ("var-back-squat", 225, 265, 5, 5, {"down": 3, "hold": 1, "up": 1}),
        ("var-front-squat", 155, 185, 5, 5, {"down": 3, "hold": 2, "up": 1}),
        ("var-barbell-row", 115, 155, 8, 8, {"down": 2, "hold": 0, "up": 1}),
        ("var-lat-pulldown", 100, 135, 10, 10, {"down": 2, "hold": 1, "up": 1}),
    ],
    "press_shoulder": [
        ("var-bench-press", 155, 195, 5, 5, {"down": 2, "hold": 1, "up": 1}),
        ("var-overhead-press", 95, 125, 5, 5, {"down": 2, "hold": 1, "up": 1}),
        ("var-lateral-raise", 15, 25, 12, 12, {"down": 2, "hold": 0, "up": 1}),
        ("var-face-pull", 30, 50, 15, 15, {"down": 2, "hold": 1, "up": 1}),
    ],
    "deadlift_arm": [
        ("var-conventional-deadlift", 275, 335, 3, 3, {"down": 2, "hold": 0, "up": 1}),
        ("var-romanian-deadlift", 185, 225, 8, 8, {"down": 3, "hold": 1, "up": 1}),
        ("var-barbell-curl", 40, 60, 10, 10, {"down": 2, "hold": 1, "up": 1}),
        ("var-tricep-pushdown", 40, 60, 10, 10, {"down": 2, "hold": 1, "up": 1}),
    ],
    "back_arm_accessory": [
        ("var-seated-cable-row", 90, 130, 10, 10, {"down": 2, "hold": 1, "up": 1}),
        ("var-lat-pulldown", 100, 140, 10, 10, {"down": 2, "hold": 1, "up": 1}),
        ("var-hammer-curl", 25, 40, 10, 10, {"down": 2, "hold": 0, "up": 1}),
        ("var-incline-bench", 135, 165, 8, 8, {"down": 2, "hold": 1, "up": 1}),
    ],
}

# ── Warmup / finisher options ───────────────────────────────────────────
WARMUPS = [
    "5 min rowing",
    "Band pull-aparts",
    "Empty bar squats",
    "Foam rolling + dynamic stretches",
    "Jump rope 3 min",
    "Arm circles + band dislocates",
    "Goblet squats with light weight",
    "Hip 90/90 stretches",
]

FINISHERS = [
    "3 rounds of pull-ups and dips",
    "Farmer's walks 3x40m",
    "Plank hold 3x45s",
    "Bodyweight lunges 2x10 each",
    "Battle ropes 3x30s",
    "Hanging leg raises 3x10",
    None,  # some sessions have no finisher
    None,
]

NOTES_POOL = [
    "Felt strong today",
    "Smooth reps",
    "Grinded the last rep",
    "New rep PR!",
    "Easy sets",
    "Good volume session",
    "Felt a bit tight",
    "Warm-up sets felt heavy",
    "Locked in today",
    "Solid session overall",
    "",  # some sets have no notes
    "",
    "",
]

LOG_NOTES = [
    "Great session today. Felt energized and strong.",
    "Deadlifts felt heavy but got through it.",
    "New PR on back squat! Let's go!",
    "Good volume day. Shoulders were pumped.",
    "Arms were toast after this one.",
    "Back session was solid. Feeling progress.",
    "Easy recovery day. Focused on form.",
    "Tough session but pushed through.",
    "Feeling stronger every week.",
    "Good intensity today. RPE was high.",
    "Solid session. Nutrition was on point.",
    "Fatigue from yesterday but managed good sets.",
]

VIBES = [3, 4, 4, 5, 5, 5, 4, 3, 4, 5]


def get_lift_lookup():
    return {c["id"]: c for c in CATEGORIES}


def generate_sets(start_date, end_date, lift_lookup):
    """Generate sets across the date range using the session templates."""
    sets = []
    set_id = 1

    # Generate training dates (Mon/Wed/Fri/Sat pattern)
    training_dates = []
    current = start_date
    # Find first Monday
    while current.weekday() != 0:
        current += timedelta(days=1)

    week = 0
    while current <= end_date and week < 8:
        # Mon: squat_back
        if current <= end_date:
            training_dates.append((current, "squat_back"))
        # Wed: press_shoulder
        wed = current + timedelta(days=2)
        if wed <= end_date:
            training_dates.append((wed, "press_shoulder"))
        # Fri: deadlift_arm
        fri = current + timedelta(days=4)
        if fri <= end_date:
            training_dates.append((fri, "deadlift_arm"))
        # Sat: back_arm_accessory
        sat = current + timedelta(days=5)
        if sat <= end_date:
            training_dates.append((sat, "back_arm_accessory"))

        current += timedelta(days=7)
        week += 1

    total_weeks = max(1, week)

    for session_idx, (date, session_type) in enumerate(training_dates):
        template = SESSION_TEMPLATES[session_type]
        week_num = session_idx // 4  # approximate week number

        for var_id, start_w, end_w, start_r, end_r, tempo in template:
            # Progressive overload: interpolate weight over weeks
            progress = week_num / max(1, total_weeks - 1)
            weight = start_w + (end_w - start_w) * progress
            # Add small random variation (±5 lbs)
            weight = max(start_w, weight + random.uniform(-5, 5))
            # Round to nearest 5 lbs for realism
            weight = round(weight / 5) * 5

            reps = start_r  # keep reps constant for simplicity

            # Generate 3-5 sets per exercise
            num_sets = random.choice([3, 3, 4, 4, 5])
            for s in range(num_sets):
                # Slight weight variation between sets
                set_weight = weight + random.choice([0, 0, 0, -5, -10])
                set_weight = max(0, set_weight)

                # Reps vary slightly on later sets
                set_reps = reps
                if s >= 2:
                    set_reps = max(1, reps + random.choice([-1, 0, 0]))

                sets.append({
                    "id": f"set-{set_id}",
                    "variationId": var_id,
                    "weight": float(set_weight),
                    "reps": set_reps,
                    "tempo": tempo,
                    "date": int(datetime.combine(date, datetime.min.time()).timestamp() * 1000),
                    "notes": random.choice(NOTES_POOL),
                    "rpe": random.choice([6, 7, 7, 8, 8, 8, 9, 9, 10]),
                    "mer": 0,
                })
                set_id += 1

    return sets, training_dates


def generate_workouts(training_dates):
    """Generate workout entries for each training date."""
    workouts = []
    seen_dates = set()

    for date, session_type in training_dates:
        date_str = date.isoformat()
        if date_str in seen_dates:
            continue
        seen_dates.add(date_str)

        workouts.append({
            "id": f"workout-{len(workouts) + 1}",
            "date": date_str,
            "warmup": random.choice(WARMUPS),
            "finisher": random.choice(FINISHERS),
            "exercises": [],
        })

    return workouts


def generate_lifting_logs(training_dates):
    """Generate lifting log entries for each training date."""
    logs = []
    seen_dates = set()

    for date, session_type in training_dates:
        date_str = date.isoformat()
        if date_str in seen_dates:
            continue
        seen_dates.add(date_str)

        logs.append({
            "id": f"log-{len(logs) + 1}",
            "date": date_str,
            "notes": random.choice(LOG_NOTES),
            "vibe": random.choice(VIBES),
        })

    return logs


def generate_backup():
    random.seed(42)  # reproducible output

    # Date range: 2 months back from today
    end_date = datetime.now().date()
    start_date = end_date - timedelta(days=56)

    lift_lookup = get_lift_lookup()

    # Build variations with full lift objects
    variations = []
    for v in VARIATIONS:
        lift = lift_lookup[v["lift"]]
        variations.append({
            "id": v["id"],
            "lift": {"id": lift["id"], "name": lift["name"]},
            "name": v["name"],
            "reps": v["reps"],
            "favourite": v["favourite"],
            "notes": v.get("notes", ""),
            "bodyWeight": v["bodyWeight"],
        })

    # Generate training data
    sets, training_dates = generate_sets(start_date, end_date, lift_lookup)
    workouts = generate_workouts(training_dates)
    lifting_logs = generate_lifting_logs(training_dates)

    backup = {
        "lifts": CATEGORIES,
        "variations": variations,
        "sets": sets,
        "workouts": workouts,
        "exercises": [],
        "liftingLogs": lifting_logs,
    }

    return backup


if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    output_path = (
        sys.argv[1]
        if len(sys.argv) > 1
        else os.path.join(project_root, "build/generated/test-data/screenshot_test_backup.json")
    )

    backup = generate_backup()

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w") as f:
        json.dump(backup, f, indent=4)

    # Print summary
    print(f"Generated {output_path}")
    print(f"  Categories: {len(backup['lifts'])}")
    print(f"  Variations: {len(backup['variations'])}")
    print(f"  Sets: {len(backup['sets'])}")
    print(f"  Workouts: {len(backup['workouts'])}")
    print(f"  Lifting Logs: {len(backup['liftingLogs'])}")

    # Show date range
    dates = [w["date"] for w in backup["workouts"]]
    print(f"  Date range: {min(dates)} to {max(dates)}")
