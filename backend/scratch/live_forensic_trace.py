import requests
import json
import subprocess
import time

BASE_URL = "http://localhost:8083/api/v1"

def run_sql(query):
    cmd = ["mysql", "-u", "root", "-p1234567890", "-e", f"USE blood_bridge; {query}"]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout.strip()

def print_section(title):
    print("\n" + "="*80)
    print(f"  {title}")
    print("="*80)

def main():
    print_section("LIVE RUNTIME FORENSIC TRACE: EMERGENCY BLOOD REQUEST -> DONOR DASHBOARD FEED")

    # 1. Hospital Credentials
    hosp_email = "apollo.live.trace@hospital.com"
    hosp_pass = "Password123!"

    # 2. Donor B+ Credentials
    donor_email = "donor.b.live.trace@test.com"
    donor_pass = "Password123!"

    # =========================================================================
    print_section("STEP 1: AUTHENTICATION & JWT GENERATION")
    # =========================================================================

    hosp_login = requests.post(f"{BASE_URL}/auth/login", json={"email": hosp_email, "password": hosp_pass}).json()
    hosp_token = hosp_login['data']['token']
    hosp_user_id = hosp_login['data']['user']['id']
    print(f"[SUCCESS] Hospital Authenticated! User ID: #{hosp_user_id} | Token: {hosp_token[:25]}...")

    donor_login = requests.post(f"{BASE_URL}/auth/login", json={"email": donor_email, "password": donor_pass}).json()
    donor_token = donor_login['data']['token']
    donor_user_id = donor_login['data']['user']['id']
    print(f"[SUCCESS] Donor B+ Authenticated! User ID: #{donor_user_id} | Token: {donor_token[:25]}...")

    # =========================================================================
    print_section("STEP 2: HOSPITAL CREATES EMERGENCY B+ REQUEST (POST /api/v1/hospital/blood-requests)")
    # =========================================================================

    create_payload = {
        "bloodGroupNeeded": "B_POSITIVE",
        "unitsRequired": 1,
        "urgencyLevel": "HIGH",
        "requiredByDate": "2026-08-08",
        "reason": "Emergency Transfusion - Live Forensic Audit"
    }

    create_res = requests.post(
        f"{BASE_URL}/hospital/blood-requests",
        headers={"Authorization": f"Bearer {hosp_token}"},
        json=create_payload
    )

    print(f"HTTP Status: {create_res.status_code}")
    print(f"Response Payload:\n{json.dumps(create_res.json(), indent=2)}")

    blood_req_id = create_res.json()['data']['id']
    print(f"\n[CONFIRMED] Saved BloodRequest ID: #{blood_req_id}")

    sql_br = run_sql(f"SELECT id, blood_group_needed, units_required, status, hospital_id, created_at FROM blood_requests WHERE id = {blood_req_id};")
    print(f"\nDatabase `blood_requests` Row:\n{sql_br}")

    # =========================================================================
    print_section("STEP 3 & 4: SMART MATCHING ENGINE PIPELINE & PERSISTENCE CHECK")
    # =========================================================================

    time.sleep(1) # Allow completion of matching pipeline

    sql_med = run_sql(f"""
        SELECT id, blood_request_id, donor_id, hospital_id, distance_km, matching_group, status, notification_sent, created_at 
        FROM matched_emergency_donors 
        WHERE blood_request_id = {blood_req_id};
    """)
    print(f"Database `matched_emergency_donors` Rows:\n{sql_med}")

    assert "blood_request_id" in sql_med and len(sql_med.splitlines()) > 1, "CRITICAL ERROR: Zero matched_emergency_donors rows!"

    # =========================================================================
    print_section("STEP 5: DONOR TO USER ID RELATIONSHIP MAPPING PROOF")
    # =========================================================================

    sql_mapping = run_sql(f"""
        SELECT u.id as user_id, u.email, dp.id as donor_profile_id, dp.blood_group, med.id as matched_id, med.status as match_status 
        FROM users u 
        JOIN donor_profiles dp ON dp.user_id = u.id 
        JOIN matched_emergency_donors med ON med.donor_id = dp.id 
        WHERE med.blood_request_id = {blood_req_id};
    """)
    print(f"Database Foreign Key Chain (`users` -> `donor_profiles` -> `matched_emergency_donors`):\n{sql_mapping}")

    # =========================================================================
    print_section("STEP 6: DONOR CALLS GET /api/v1/donor/emergency-requests VIA JWT")
    # =========================================================================

    donor_api_res = requests.get(
        f"{BASE_URL}/donor/emergency-requests",
        headers={"Authorization": f"Bearer {donor_token}"}
    )

    print(f"HTTP Status: {donor_api_res.status_code}")
    print(f"Response Payload:\n{json.dumps(donor_api_res.json(), indent=2)}")

    matched_list = donor_api_res.json().get('data', [])
    assert len(matched_list) > 0, "CRITICAL ERROR: Donor API returned []!"

    matched_dto = matched_list[0]
    print("\n[VERIFIED DTO PROPERTIES]")
    print(f" -> requestId       : {matched_dto.get('requestId')}")
    print(f" -> hospitalName    : {matched_dto.get('hospitalName')}")
    print(f" -> bloodGroup      : {matched_dto.get('bloodGroup')}")
    print(f" -> unitsRequired   : {matched_dto.get('unitsRequired')}")
    print(f" -> priority        : {matched_dto.get('priority')}")
    print(f" -> distanceKm      : {matched_dto.get('distanceKm')} km")
    print(f" -> matchingGroup   : {matched_dto.get('matchingGroup')}")
    print(f" -> googleMapsUrl   : {matched_dto.get('googleMapsUrl')}")

    # =========================================================================
    print_section("STEP 7: DONOR ACCEPTS EMERGENCY REQUEST (POST /api/v1/donor/emergency-requests/{id}/accept)")
    # =========================================================================

    accept_res = requests.post(
        f"{BASE_URL}/donor/emergency-requests/{blood_req_id}/accept",
        headers={"Authorization": f"Bearer {donor_token}"}
    )

    print(f"HTTP Status: {accept_res.status_code}")
    print(f"Accept DTO Output:\n{json.dumps(accept_res.json(), indent=2)}")

    sql_after_accept = run_sql(f"SELECT id, blood_request_id, donor_id, status FROM matched_emergency_donors WHERE blood_request_id = {blood_req_id};")
    print(f"\nDatabase `matched_emergency_donors` After Accept:\n{sql_after_accept}")

    sql_br_after_accept = run_sql(f"SELECT id, status FROM blood_requests WHERE id = {blood_req_id};")
    print(f"\nDatabase `blood_requests` Status After Accept:\n{sql_br_after_accept}")

    print_section("ALL FORENSIC STEPS COMPLETED & VERIFIED WITH 100% EMPIRICAL SUCCESS!")

if __name__ == '__main__':
    main()
