import os
import sys
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

from reportlab.lib.pagesizes import landscape, letter
from reportlab.lib import colors
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, Table, TableStyle, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle

DOCS_DIR = os.path.abspath("docs")
SCREENSHOTS_DIR = os.path.join(DOCS_DIR, "screenshots")
PPTX_PATH = os.path.join(DOCS_DIR, "Review1_Presentation.pptx")
PDF_PATH = os.path.join(DOCS_DIR, "Review1_Presentation.pdf")

# Palette tokens (Warm White-Peach theme)
BG_COLOR = RGBColor(255, 251, 248)       # #FFFBF8
SURFACE_COLOR = RGBColor(255, 255, 255)  # #FFFFFF
PEACH_TINT = RGBColor(255, 245, 238)     # #FFF5EE
PEACH_BORDER = RGBColor(243, 227, 216)   # #F3E3D8
PEACH_PRIMARY = RGBColor(232, 144, 107)  # #E8906B
PEACH_DARK = RGBColor(201, 111, 74)      # #C96F4A
TEXT_MAIN = RGBColor(58, 43, 37)         # #3A2B25
TEXT_MUTED = RGBColor(138, 114, 104)     # #8A7268
SUCCESS_COLOR = RGBColor(127, 168, 139)  # #7FA88B

def set_slide_background(slide, prs):
    background = slide.background
    fill = background.fill
    fill.solid()
    fill.fore_color.rgb = BG_COLOR

def add_header(slide, title_text, category="ONLINE HEALTHCARE MANAGEMENT SYSTEM • REVIEW 1"):
    # Category / Pill
    cat_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11), Inches(0.35))
    tf_cat = cat_box.text_frame
    tf_cat.word_wrap = True
    tf_cat.margin_left = tf_cat.margin_top = tf_cat.margin_right = tf_cat.margin_bottom = 0
    p_cat = tf_cat.paragraphs[0]
    p_cat.text = category.upper()
    p_cat.font.name = "Arial"
    p_cat.font.size = Pt(10)
    p_cat.font.bold = True
    p_cat.font.color.rgb = PEACH_DARK

    # Title
    title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.75), Inches(11.5), Inches(0.65))
    tf_title = title_box.text_frame
    tf_title.word_wrap = True
    tf_title.margin_left = tf_title.margin_top = tf_title.margin_right = tf_title.margin_bottom = 0
    p_title = tf_title.paragraphs[0]
    p_title.text = title_text
    p_title.font.name = "Georgia"
    p_title.font.size = Pt(22)
    p_title.font.bold = True
    p_title.font.color.rgb = TEXT_MAIN

def add_card(slide, left, top, width, height, bg_rgb=SURFACE_COLOR, border_rgb=PEACH_BORDER):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = bg_rgb
    shape.line.color.rgb = border_rgb
    shape.line.width = Pt(1)
    return shape

def add_bullet_list(tf, items, title=None, bullet_size=12, text_color=TEXT_MAIN):
    if title:
        p0 = tf.paragraphs[0]
        p0.text = title
        p0.font.name = "Georgia"
        p0.font.size = Pt(bullet_size + 2)
        p0.font.bold = True
        p0.font.color.rgb = PEACH_DARK
        p0.space_after = Pt(8)
    
    first = True if not title else False
    for item in items:
        if first:
            p = tf.paragraphs[0]
            first = False
        else:
            p = tf.add_paragraph()
        p.text = "• " + item
        p.font.name = "Arial"
        p.font.size = Pt(bullet_size)
        p.font.color.rgb = text_color
        p.space_after = Pt(6)

def build_presentation():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # =========================================================================
    # SLIDE 1: Title Slide
    # =========================================================================
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_background(s1, prs)

    card1 = add_card(s1, Inches(1.2), Inches(1.0), Inches(10.933), Inches(5.5), SURFACE_COLOR)
    tb1 = s1.shapes.add_textbox(Inches(1.6), Inches(1.4), Inches(10.133), Inches(4.7))
    tf1 = tb1.text_frame
    tf1.word_wrap = True

    p = tf1.paragraphs[0]
    p.text = "PROJECT REVIEW 1 • SUBMISSION PACK"
    p.font.name = "Arial"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = PEACH_DARK
    p.space_after = Pt(14)

    p = tf1.add_paragraph()
    p.text = "Online Healthcare Management System"
    p.font.name = "Georgia"
    p.font.size = Pt(32)
    p.font.bold = True
    p.font.color.rgb = TEXT_MAIN
    p.space_after = Pt(8)

    p = tf1.add_paragraph()
    p.text = "Enterprise Java Web Architecture (Jakarta Servlet 6.0 + JSP 3.1 + JDBC + MySQL 8.0)"
    p.font.name = "Arial"
    p.font.size = Pt(16)
    p.font.color.rgb = PEACH_DARK
    p.space_after = Pt(28)

    p = tf1.add_paragraph()
    p.text = "Presenter: Deepanshu (Lead Developer & System Architect)"
    p.font.name = "Arial"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = TEXT_MAIN
    p.space_after = Pt(6)

    p = tf1.add_paragraph()
    p.text = "Course: Java Web-Based Application Development  |  Evaluation Phase: Review 1 Assessment"
    p.font.name = "Arial"
    p.font.size = Pt(13)
    p.font.color.rgb = TEXT_MUTED
    p.space_after = Pt(6)

    p = tf1.add_paragraph()
    p.text = "Rubric Domains: Problem Understanding & Architecture (8) • Core Java (10) • Database/JDBC (8) • Servlets & Web (7)"
    p.font.name = "Arial"
    p.font.size = Pt(12)
    p.font.color.rgb = SUCCESS_COLOR

    # =========================================================================
    # SLIDE 2: Problem Statement & Objectives
    # =========================================================================
    s2 = prs.slides.add_slide(blank_layout)
    set_slide_background(s2, prs)
    add_header(s2, "Problem Statement & Strategic Engineering Objectives")

    # Left card: The Problem
    add_card(s2, Inches(0.8), Inches(1.6), Inches(5.6), Inches(5.3))
    tb2_left = s2.shapes.add_textbox(Inches(1.1), Inches(1.9), Inches(5.0), Inches(4.7))
    tf2_l = tb2_left.text_frame
    tf2_l.word_wrap = True
    add_bullet_list(tf2_l, [
        "Traditional clinic operations suffer from manual, paper-heavy scheduling leading to double-booked consultations.",
        "Fragmented patient medical records across visits result in missing prescription histories and compromised clinical care.",
        "Lack of unified role-based authorization exposes sensitive patient EHR records to unauthorized staff.",
        "Inefficient database connectivity models without pooling produce connection leaks and high latency under clinic peak traffic."
    ], title="The Clinical Challenge", bullet_size=13)

    # Right card: Objectives
    add_card(s2, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.3))
    tb2_right = s2.shapes.add_textbox(Inches(7.1), Inches(1.9), Inches(5.1), Inches(4.7))
    tf2_r = tb2_right.text_frame
    tf2_r.word_wrap = True
    add_bullet_list(tf2_r, [
        "100% Pure Java Enterprise Stack: Build a robust Jakarta Servlet 6.0 + JSP system without heavy black-box frameworks.",
        "Normalized 3NF Relational Architecture: Guarantee data integrity across 12 relational tables with foreign keys and indexes.",
        "High-Performance Connection Pooling: Implement HikariCP connection leasing with try-with-resources and PreparedStatements.",
        "ACID Concurrency Safety: Keyed ReentrantLock slot synchronization preventing race conditions during live patient booking.",
        "Responsive, Accessible Design System: Warm peach design tokens ensuring seamless operation across 375px mobile and desktop."
    ], title="Review 1 Objectives", bullet_size=13)

    # =========================================================================
    # SLIDE 3: User Roles & Key Features
    # =========================================================================
    s3 = prs.slides.add_slide(blank_layout)
    set_slide_background(s3, prs)
    add_header(s3, "User Roles & Multi-Stakeholder Feature Matrix")

    # 3 Cards for Admin, Doctor, Patient
    roles = [
        ("👑 Administrator", [
            "Central clinic user and doctor registry",
            "Real-time operational throughput KPI cards",
            "Immutable audit security log monitoring",
            "System settings configuration & demo reset",
            "Live JDBC database connectivity diagnostics"
        ], Inches(0.8)),
        ("🩺 Consulting Doctor", [
            "Weekly recurring availability roster manager",
            "Daily appointment queue & triage status",
            "EHR medical diagnosis and prescription entry",
            "Patient historical clinical chart inspection",
            "Star rating feedback review tracking"
        ], Inches(4.8)),
        ("👤 Registered Patient", [
            "Self-service account registration with BCrypt",
            "Specialist directory search & filter catalog",
            "Real-time slot reservation with locking",
            "Personal prescription and EHR history view",
            "Verified consultation feedback and reviews"
        ], Inches(8.8))
    ]

    for title, feats, left_pos in roles:
        add_card(s3, left_pos, Inches(1.6), Inches(3.7), Inches(5.3))
        tb = s3.shapes.add_textbox(left_pos + Inches(0.2), Inches(1.9), Inches(3.3), Inches(4.7))
        tf = tb.text_frame
        tf.word_wrap = True
        add_bullet_list(tf, feats, title=title, bullet_size=12)

    # =========================================================================
    # SLIDE 4: Tech Stack & Why
    # =========================================================================
    s4 = prs.slides.add_slide(blank_layout)
    set_slide_background(s4, prs)
    add_header(s4, "Enterprise Technology Stack & Engineering Rationale")

    stack_items = [
        ("☕ Core Java 17 LTS", "Modern language features (Records, pattern matching, strong typing, multithreading primitives), memory safety, and high server-side execution performance."),
        ("⚙️ Jakarta Servlet 6.0", "Provides predictable low-overhead HTTP lifecycle control (init, service, destroy), clean request dispatching, and extensible Filter interceptor chains."),
        ("🎨 Jakarta JSP 3.1 & JSTL 3.0", "Declarative server-side view rendering, dynamic templating, automatic XSS HTML escaping, and separation of UI markup from backend logic."),
        ("🏊 JDBC & HikariCP 5.1", "Enterprise connection pooling managing leased connections; eliminates connection leak hazards with 100% PreparedStatement injection defense."),
        ("🗄️ MySQL 8.0+ / 9.x (InnoDB)", "ACID-compliant relational persistence, foreign key cascades, transaction isolation, and B-Tree indexing on high-frequency lookup columns."),
        ("🚀 Embedded Tomcat 10.1", "Enables zero-configuration, 1-command local execution (mvn compile exec:java) while maintaining standard production WAR deployment portability.")
    ]

    for i, (tech, reason) in enumerate(stack_items):
        row = i // 2
        col = i % 2
        card_l = Inches(0.8 + col * 5.9)
        card_t = Inches(1.6 + row * 1.8)
        add_card(s4, card_l, card_t, Inches(5.7), Inches(1.6))
        tb = s4.shapes.add_textbox(card_l + Inches(0.2), card_t + Inches(0.15), Inches(5.3), Inches(1.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p0 = tf.paragraphs[0]
        p0.text = tech
        p0.font.name = "Georgia"
        p0.font.size = Pt(14)
        p0.font.bold = True
        p0.font.color.rgb = PEACH_DARK
        p0.space_after = Pt(4)
        p1 = tf.add_paragraph()
        p1.text = reason
        p1.font.name = "Arial"
        p1.font.size = Pt(11)
        p1.font.color.rgb = TEXT_MAIN

    # =========================================================================
    # SLIDE 5: System Architecture (MVC Layered Diagram)
    # =========================================================================
    s5 = prs.slides.add_slide(blank_layout)
    set_slide_background(s5, prs)
    add_header(s5, "Layered MVC Architecture & Security Interceptor Pipeline")

    arch_img = os.path.join(DOCS_DIR, "architecture-diagram.png")
    if os.path.exists(arch_img):
        s5.shapes.add_picture(arch_img, Inches(0.8), Inches(1.6), Inches(7.5), Inches(5.3))
    
    add_card(s5, Inches(8.5), Inches(1.6), Inches(4.0), Inches(5.3))
    tb5 = s5.shapes.add_textbox(Inches(8.7), Inches(1.8), Inches(3.6), Inches(4.9))
    tf5 = tb5.text_frame
    tf5.word_wrap = True
    add_bullet_list(tf5, [
        "1. Interceptor Filter Chain: Requests pass through Encoding, Logging, Tunnel, Auth, and Role-Based Access Control filters.",
        "2. Controller Dispatch: Jakarta Servlets parse inputs, validate CSRF tokens, and invoke domain services.",
        "3. Service & Transaction Layer: Orchestrates business rules, caching, and concurrent ReentrantLock slot coordination.",
        "4. Persistence DAO Layer: Parameterized PreparedStatements with generic RowMapper extraction.",
        "5. Connection Pool: Leased HikariCP JDBC connections talking to MySQL 8 InnoDB engine."
    ], title="Request Lifecycle Flow", bullet_size=12)

    # =========================================================================
    # SLIDE 6: Project Structure
    # =========================================================================
    s6 = prs.slides.add_slide(blank_layout)
    set_slide_background(s6, prs)
    add_header(s6, "Clean Modular Project Structure & Layer Packaging")

    add_card(s6, Inches(0.8), Inches(1.6), Inches(5.6), Inches(5.3))
    tb6_left = s6.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(5.2), Inches(4.9))
    tf6_l = tb6_left.text_frame
    tf6_l.word_wrap = True
    p = tf6_l.paragraphs[0]
    p.text = "Package Organization (src/main/java)"
    p.font.name = "Georgia"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = PEACH_DARK
    p.space_after = Pt(8)
    pkg_tree = [
        "com.healthcare.model — Polymorphic entities & contracts",
        "com.healthcare.dao — Generic Repository & JDBC RowMappers",
        "com.healthcare.service — Transactions, cache & logic",
        "com.healthcare.servlet — BaseServlet & HTTP controllers",
        "com.healthcare.filter — Security, RBAC & logging filters",
        "com.healthcare.listener — Lifecycle & session monitors",
        "com.healthcare.exception — Custom domain exception hierarchy",
        "com.healthcare.util — HikariCP pool, BCrypt, CSRF, PDF",
        "com.healthcare.concurrent — ReentrantLocks & schedulers"
    ]
    for item in pkg_tree:
        pi = tf6_l.add_paragraph()
        pi.text = "📁 " + item
        pi.font.name = "Arial"
        pi.font.size = Pt(11)
        pi.font.color.rgb = TEXT_MAIN
        pi.space_after = Pt(4)

    add_card(s6, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.3))
    tb6_right = s6.shapes.add_textbox(Inches(7.1), Inches(1.8), Inches(5.1), Inches(4.9))
    tf6_r = tb6_right.text_frame
    tf6_r.word_wrap = True
    add_bullet_list(tf6_r, [
        "Package-info Documentation: Every package contains an explanatory package-info.java defining architectural boundaries.",
        "Strict Layer Decoupling: Servlets never access raw JDBC directly; DAOs never handle HTTP sessions; models remain pure POJOs.",
        "Central BaseServlet: Inherits standard lifecycle logging (init, service, destroy), CSRF validation, and JSON helpers.",
        "Zero Magic Numbers: System constants encapsulated in domain enums (Role, AppointmentStatus) and configuration tables.",
        "100% SLF4J / Logback: Zero System.out / System.err across main codebase; production structured logging enabled."
    ], title="Engineering Standards & Quality", bullet_size=12)

    # =========================================================================
    # SLIDE 7: Use-Case Diagram
    # =========================================================================
    s7 = prs.slides.add_slide(blank_layout)
    set_slide_background(s7, prs)
    add_header(s7, "System Use-Case Modeling & Role Interactions")

    uc_img = os.path.join(DOCS_DIR, "use-case-diagram.png")
    if os.path.exists(uc_img):
        s7.shapes.add_picture(uc_img, Inches(0.8), Inches(1.6), Inches(8.5), Inches(5.3))
    
    add_card(s7, Inches(9.5), Inches(1.6), Inches(3.0), Inches(5.3))
    tb7 = s7.shapes.add_textbox(Inches(9.7), Inches(1.8), Inches(2.6), Inches(4.9))
    tf7 = tb7.text_frame
    tf7.word_wrap = True
    add_bullet_list(tf7, [
        "RBAC Route Guarding: URL patterns enforced by AuthFilter and RoleFilter.",
        "Principal Isolation: Patients cannot view administrative logs; doctors restricted to their patient rosters.",
        "Session Security: Session fixation protection via changeSessionId() upon authentication."
    ], title="Key Invariants", bullet_size=12)

    # =========================================================================
    # SLIDE 8: Database Design (ER Diagram)
    # =========================================================================
    s8 = prs.slides.add_slide(blank_layout)
    set_slide_background(s8, prs)
    add_header(s8, "Database Design: 3NF Normalized Relational Schema")

    er_img = os.path.join(DOCS_DIR, "er-diagram.png")
    if os.path.exists(er_img):
        s8.shapes.add_picture(er_img, Inches(0.8), Inches(1.6), Inches(6.0), Inches(5.3))
    
    add_card(s8, Inches(7.1), Inches(1.6), Inches(5.4), Inches(5.3))
    tb8 = s8.shapes.add_textbox(Inches(7.4), Inches(1.8), Inches(4.8), Inches(4.9))
    tf8 = tb8.text_frame
    tf8.word_wrap = True
    add_bullet_list(tf8, [
        "1NF Compliance: All columns atomic; vitals separated into blood pressure, pulse, and temperature.",
        "2NF Compliance: Doctor and Patient tables extend Users via 1-to-1 PK-to-FK links, eliminating partial dependencies.",
        "3NF Compliance: Transitive dependencies removed; consultation rates anchored in doctors; diagnoses in medical_records.",
        "Foreign Key Cascades: Cascade deletions on users cleanly purge doctor/patient records while preserving appointments via RESTRICT.",
        "InnoDB Storage Engine: Row-level locking and ACID transactions for multi-row booking sequences."
    ], title="3NF Schema Normalization", bullet_size=12)

    # =========================================================================
    # SLIDE 9: Table Overview & Constraints
    # =========================================================================
    s9 = prs.slides.add_slide(blank_layout)
    set_slide_background(s9, prs)
    add_header(s9, "Relational Schema Tables & Constraint Specifications")

    # Table summary card
    add_card(s9, Inches(0.8), Inches(1.6), Inches(11.7), Inches(5.3))
    tb9 = s9.shapes.add_textbox(Inches(1.1), Inches(1.8), Inches(11.1), Inches(4.9))
    tf9 = tb9.text_frame
    tf9.word_wrap = True
    
    p = tf9.paragraphs[0]
    p.text = "12-Table Enterprise Relational Schema Breakdown"
    p.font.name = "Georgia"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = PEACH_DARK
    p.space_after = Pt(8)

    tables_summary = [
        ("users (16 rows)", "PK id, UNIQUE email, role ENUM, password_hash BCrypt, failed_attempts, locked_until"),
        ("doctors (5 rows)", "PK/FK id -> users, specialization, experience, consultation_fee, room_number"),
        ("patients (10 rows)", "PK/FK id -> users, age, gender, phone, blood_group, emergency_contact"),
        ("schedules (21 rows)", "PK id, FK doctor_id, day_of_week, start_time, end_time, slot_duration_minutes"),
        ("time_slots (30 rows)", "PK id, FK doctor_id, UNIQUE (doctor_id, slot_date, start_time), is_booked, version"),
        ("appointments (11 rows)", "PK id, FKs to doctor, patient, slot; UNIQUE (slot_id); status ENUM"),
        ("medical_records (5 rows)", "PK id, FK appointment_id, FK patient_id, diagnosis, prescription, vitals"),
        ("feedback (5 rows)", "PK id, UNIQUE appointment_id, CHECK (rating >= 1 AND rating <= 5), comment"),
        ("notifications (190 rows)", "PK id, FK user_id, title, message, type, is_read, timestamp"),
        ("system_settings (11 rows)", "PK setting_key, setting_value, description (supports dynamic app properties)"),
        ("audit_logs (8 rows)", "PK id, FK user_id, action, details, ip_address, created_at (immutable trail)"),
        ("login_attempts (0 rows)", "PK id, email, ip_address, success, attempted_at (supports lockout defense)")
    ]

    for tbl, spec in tables_summary:
        pi = tf9.add_paragraph()
        pi.text = f"• {tbl}: {spec}"
        pi.font.name = "Arial"
        pi.font.size = Pt(10.5)
        pi.font.color.rgb = TEXT_MAIN
        pi.space_after = Pt(2)

    # =========================================================================
    # SLIDE 10: Database Connectivity & DB-Test Proof
    # =========================================================================
    s10 = prs.slides.add_slide(blank_layout)
    set_slide_background(s10, prs)
    add_header(s10, "Database Connectivity (JDBC) & Live /db-test Diagnostic")

    db_test_img = os.path.join(SCREENSHOTS_DIR, "db-test-desktop.png")
    if os.path.exists(db_test_img):
        s10.shapes.add_picture(db_test_img, Inches(0.8), Inches(1.6), Inches(7.5), Inches(5.3))

    add_card(s10, Inches(8.5), Inches(1.6), Inches(4.0), Inches(5.3))
    tb10 = s10.shapes.add_textbox(Inches(8.7), Inches(1.8), Inches(3.6), Inches(4.9))
    tf10 = tb10.text_frame
    tf10.word_wrap = True
    add_bullet_list(tf10, [
        "HikariCP Connection Pool: Leased connections, max pool size: 20, min idle: 5, connection timeout: 10s.",
        "Live Connectivity Proof: Ping latency: 0 ms on MySQL 9.7.1 via mysql-connector-j 8.3.0.",
        "Automated CRUD Benchmark: 4 live operations (Create -> Read -> Update -> Delete) passed in 8 ms total.",
        "Zero SQL Injection Risk: 100% java.sql.PreparedStatement query construction across all DAOs.",
        "Resource Safety: Standard try-with-resources auto-closes all Connection, Statement, and ResultSet instances."
    ], title="JDBC Verification Metrics", bullet_size=12)

    # =========================================================================
    # SLIDE 11: Core Java Concepts Implemented
    # =========================================================================
    s11 = prs.slides.add_slide(blank_layout)
    set_slide_background(s11, prs)
    add_header(s11, "Core Java Concepts (Rubric: 10 Marks)")

    core_concepts = [
        ("OOP & Polymorphism", "Polymorphic User hierarchy (Doctor, Patient, Admin) implementing Authenticatable, Bookable, and Reportable contracts with overridden getDashboardPath()."),
        ("Generics & Collections", "Generic Repository<T, ID>, functional RowMapper<T>, ApiResponse<T> DTO, thread-safe ConcurrentHashMap in SlotLockManager and DoctorCache."),
        ("Custom Exceptions", "Domain exception hierarchy with HealthcareException base, branching into AuthenticationException, ValidationException, DatabaseException, and AppointmentConflictException."),
        ("Concurrency & Locks", "SlotLockManager fine-grained ReentrantLock(true) per doctor-slot; ScheduledExecutorService in ReminderScheduler for automated patient alerts."),
        ("JDBC Transactions", "Explicit transaction boundaries (conn.setAutoCommit(false), conn.commit(), conn.rollback()) ensuring atomicity across slot booking and appointment insertion.")
    ]

    for i, (concept, desc) in enumerate(core_concepts):
        top_pos = Inches(1.6 + i * 1.05)
        add_card(s11, Inches(0.8), top_pos, Inches(11.7), Inches(0.95))
        tb = s11.shapes.add_textbox(Inches(1.0), top_pos + Inches(0.08), Inches(11.3), Inches(0.8))
        tf = tb.text_frame
        tf.word_wrap = True
        p0 = tf.paragraphs[0]
        p0.text = concept
        p0.font.name = "Georgia"
        p0.font.size = Pt(13)
        p0.font.bold = True
        p0.font.color.rgb = PEACH_DARK
        p1 = tf.add_paragraph()
        p1.text = desc
        p1.font.name = "Arial"
        p1.font.size = Pt(11)
        p1.font.color.rgb = TEXT_MAIN

    # =========================================================================
    # SLIDE 12: UI/UX Design System
    # =========================================================================
    s12 = prs.slides.add_slide(blank_layout)
    set_slide_background(s12, prs)
    add_header(s12, "Design System Architecture: Warm Peach Aesthetic")

    # Tokens card
    add_card(s12, Inches(0.8), Inches(1.6), Inches(5.6), Inches(5.3))
    tb12_l = s12.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(5.2), Inches(4.9))
    tf12_l = tb12_l.text_frame
    tf12_l.word_wrap = True
    add_bullet_list(tf12_l, [
        "Warm Palette Tokens: Curated HSL colors (--bg: #FFFBF8, --peach-500: #E8906B, --text: #3A2B25); zero harsh neon tones.",
        "Typography Hierarchy: Fraunces (refined modern editorial serif) for headings + Plus Jakarta Sans for high-legibility body UI.",
        "Bento Grid Component System: Modular card spans (col-span-3, col-span-6) supporting animated KPI counters and activity streams.",
        "Micro-Interactions: Smooth cubic-bezier transitions, soft atmospheric background blur blobs, and tactile button hover states.",
        "Dark Mode Infrastructure: Full [data-theme='dark'] CSS variable token overrides for eye-friendly low-light clinical environments."
    ], title="Design Principles & Tokens", bullet_size=12)

    # Right Card: Component Library
    add_card(s12, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.3))
    tb12_r = s12.shapes.add_textbox(Inches(7.0), Inches(1.8), Inches(5.3), Inches(4.9))
    tf12_r = tb12_r.text_frame
    tf12_r.word_wrap = True
    add_bullet_list(tf12_r, [
        "Bento Stat Cards: Animated numeric counters, status delta indicators, and clinical category icons.",
        "Accessible Forms: Floating labels, inline input validation states, and 1-click demo credential autofill.",
        "Responsive Data Tables: Stacked card adaptation on mobile screens with badge chips for appointment statuses.",
        "Flash Toast Messages: Non-blocking notification toasts with SVG indicators for system feedback.",
        "Modal Dialogs: Accessible overlay booking prompts and medical record review drawers."
    ], title="Component Library & UX Patterns", bullet_size=12)

    # =========================================================================
    # SLIDE 13: Screenshots (Desktop Walkthrough)
    # =========================================================================
    s13 = prs.slides.add_slide(blank_layout)
    set_slide_background(s13, prs)
    add_header(s13, "Desktop User Experience & Multi-Role Dashboards")

    shots = [
        ("Landing Page", os.path.join(SCREENSHOTS_DIR, "landing-desktop.png"), Inches(0.8), Inches(1.6)),
        ("Admin Dashboard", os.path.join(SCREENSHOTS_DIR, "admin-dashboard-desktop.png"), Inches(6.8), Inches(1.6)),
        ("Doctor Dashboard", os.path.join(SCREENSHOTS_DIR, "doctor-dashboard-desktop.png"), Inches(0.8), Inches(4.3)),
        ("Patient Dashboard", os.path.join(SCREENSHOTS_DIR, "patient-dashboard-desktop.png"), Inches(6.8), Inches(4.3))
    ]

    for title, img_path, left_pos, top_pos in shots:
        if os.path.exists(img_path):
            s13.shapes.add_picture(img_path, left_pos, top_pos, Inches(5.7), Inches(2.5))

    # =========================================================================
    # SLIDE 14: Responsiveness (Mobile vs Desktop)
    # =========================================================================
    s14 = prs.slides.add_slide(blank_layout)
    set_slide_background(s14, prs)
    add_header(s14, "Multi-Device Responsiveness (375px Mobile vs 1280px Desktop)")

    d_img = os.path.join(SCREENSHOTS_DIR, "landing-desktop.png")
    m_img = os.path.join(SCREENSHOTS_DIR, "landing-mobile.png")
    m_dash = os.path.join(SCREENSHOTS_DIR, "admin-dashboard-mobile.png")

    if os.path.exists(d_img):
        s14.shapes.add_picture(d_img, Inches(0.8), Inches(1.6), Inches(6.8), Inches(3.8))
    if os.path.exists(m_img):
        s14.shapes.add_picture(m_img, Inches(7.9), Inches(1.6), Inches(2.3), Inches(5.3))
    if os.path.exists(m_dash):
        s14.shapes.add_picture(m_dash, Inches(10.4), Inches(1.6), Inches(2.3), Inches(5.3))

    add_card(s14, Inches(0.8), Inches(5.6), Inches(6.8), Inches(1.3))
    tb14 = s14.shapes.add_textbox(Inches(1.0), Inches(5.7), Inches(6.4), Inches(1.1))
    tf14 = tb14.text_frame
    tf14.word_wrap = True
    add_bullet_list(tf14, [
        "Zero Horizontal Overflow: Verified at 375px, 768px, and 1280px viewports (scrollWidth == innerWidth).",
        "Adaptive Navigation: Desktop persistent sidebar automatically collapses to mobile overlay drawer."
    ], bullet_size=11)

    # =========================================================================
    # SLIDE 15: Servlets & Web Integration
    # =========================================================================
    s15 = prs.slides.add_slide(blank_layout)
    set_slide_background(s15, prs)
    add_header(s15, "Jakarta Servlets & Web Integration (Rubric: 7 Marks)")

    add_card(s15, Inches(0.8), Inches(1.6), Inches(5.6), Inches(5.3))
    tb15_l = s15.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(5.2), Inches(4.9))
    tf15_l = tb15_l.text_frame
    tf15_l.word_wrap = True
    add_bullet_list(tf15_l, [
        "Jakarta Servlet 6.0 Lifecycle: Explicit overrides of init(), service(), doGet(), doPost(), and destroy() in BaseServlet with SLF4J audit logging.",
        "Session State Management: HTTP sessions store authenticated User principals with 30-minute inactivity timeouts.",
        "Session Fixation Defense: Calls req.changeSessionId() immediately upon credential verification to block session fixation exploits.",
        "Flash Feedback Messaging: Session-scoped feedback attributes automatically cleared post-render."
    ], title="Servlet Lifecycle & Sessions", bullet_size=12)

    add_card(s15, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.3))
    tb15_r = s15.shapes.add_textbox(Inches(7.0), Inches(1.8), Inches(5.3), Inches(4.9))
    tf15_r = tb15_r.text_frame
    tf15_r.word_wrap = True
    add_bullet_list(tf15_r, [
        "EncodingFilter: Forces request and response encoding to UTF-8.",
        "LoggingFilter: Measures millisecond latency and logs access trails.",
        "TunnelSecurityFilter: Detects reverse proxies (Cloudflare/ngrok), attaching Secure and SameSite=Lax flags.",
        "AuthFilter: Intercepts private route paths (/admin/*, /doctor/*, /patient/*), redirecting unauthenticated users to /login.",
        "RoleFilter: Strict Role-Based Access Control enforcing role boundaries."
    ], title="Servlet Filter Pipeline & Security", bullet_size=12)

    # =========================================================================
    # SLIDE 16: Progress & Review 2 Plan
    # =========================================================================
    s16 = prs.slides.add_slide(blank_layout)
    set_slide_background(s16, prs)
    add_header(s16, "Current Accomplishments & Review 2 Development Roadmap")

    add_card(s16, Inches(0.8), Inches(1.6), Inches(5.6), Inches(5.3))
    tb16_l = s16.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(5.2), Inches(4.9))
    tf16_l = tb16_l.text_frame
    tf16_l.word_wrap = True
    add_bullet_list(tf16_l, [
        "✓ 100% Modular Layering: model, dao, service, servlet, filter, listener, exception, util, concurrent with package-info.java.",
        "✓ Normalized 3NF Database: 12 relational tables with foreign keys, indexes, and seeded clinical records.",
        "✓ HikariCP JDBC Pool: Connection pooling with PreparedStatement and /db-test diagnostic benchmark.",
        "✓ Multi-Role Authentication: BCrypt hashing, session fixation prevention, and 3 role dashboard shells.",
        "✓ Verified Responsiveness: Tested across 375px, 768px, and 1280px without horizontal scroll."
    ], title="Review 1 Milestones Achieved (100%)", bullet_size=12)

    add_card(s16, Inches(6.8), Inches(1.6), Inches(5.7), Inches(5.3))
    tb16_r = s16.shapes.add_textbox(Inches(7.0), Inches(1.8), Inches(5.3), Inches(4.9))
    tf16_r = tb16_r.text_frame
    tf16_r.word_wrap = True
    add_bullet_list(tf16_r, [
        "→ Advanced PDF Prescription Generation: Automated OpenPDF clinical summary download with doctor digital sign-off.",
        "→ Telehealth Video Consultation Integration: WebRTC peer video room links embedded in active appointment consultations.",
        "→ Automated Notification Dispatch: Multi-channel SMS and email reminders via Background ScheduledExecutorService.",
        "→ Clinical Analytics Visualizations: Interactive Chart.js charts for clinic revenue and doctor consultation volumes.",
        "→ End-to-End JUnit 5 Integration Testing: Automated DAO transaction rollbacks and mock servlet testing."
    ], title="Review 2 Development Roadmap", bullet_size=12)

    # =========================================================================
    # SLIDE 17: Repository, Verification & Demo Access
    # =========================================================================
    s17 = prs.slides.add_slide(blank_layout)
    set_slide_background(s17, prs)
    add_header(s17, "Project Repository, Live Demo Access & Verification")

    qr_img = os.path.join(DOCS_DIR, "github-qr.png")
    if os.path.exists(qr_img):
        s17.shapes.add_picture(qr_img, Inches(1.2), Inches(2.0), Inches(3.2), Inches(3.2))

    tb_qr = s17.shapes.add_textbox(Inches(1.0), Inches(5.4), Inches(3.6), Inches(1.2))
    tf_qr = tb_qr.text_frame
    tf_qr.word_wrap = True
    p = tf_qr.paragraphs[0]
    p.text = "Scan QR for Public GitHub Repo"
    p.font.name = "Arial"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = PEACH_DARK
    p.alignment = PP_ALIGN.CENTER
    p2 = tf_qr.add_paragraph()
    p2.text = "https://github.com/ParvPrakash/healthcare-management-system-"
    p2.font.name = "Arial"
    p2.font.size = Pt(9)
    p2.font.color.rgb = TEXT_MUTED
    p2.alignment = PP_ALIGN.CENTER

    add_card(s17, Inches(5.2), Inches(1.6), Inches(7.3), Inches(5.3))
    tb17_r = s17.shapes.add_textbox(Inches(5.5), Inches(1.8), Inches(6.7), Inches(4.9))
    tf17_r = tb17_r.text_frame
    tf17_r.word_wrap = True

    p = tf17_r.paragraphs[0]
    p.text = "Pre-Seeded Demo Credentials (1-Click Auto-Fill)"
    p.font.name = "Georgia"
    p.font.size = Pt(15)
    p.font.bold = True
    p.font.color.rgb = PEACH_DARK
    p.space_after = Pt(10)

    creds = [
        ("👑 Administrator", "admin@carefirst.com", "Admin@123", "User management, analytics, audit logs, settings, /db-test"),
        ("🩺 Doctor", "doctor.sarah@carefirst.com", "Doctor@123", "Schedule planner, appointment queue, EHR records, feedback"),
        ("👤 Patient", "john.doe@example.com", "Patient@123", "Specialist doctor catalog, live slot booking, prescriptions")
    ]

    for role_name, em, pw, desc in creds:
        pi = tf17_r.add_paragraph()
        pi.text = f"{role_name}: {em}  |  Password: {pw}"
        pi.font.name = "Arial"
        pi.font.size = Pt(12)
        pi.font.bold = True
        pi.font.color.rgb = TEXT_MAIN
        
        pi2 = tf17_r.add_paragraph()
        pi2.text = f"Features: {desc}"
        pi2.font.name = "Arial"
        pi2.font.size = Pt(10.5)
        pi2.font.color.rgb = TEXT_MUTED
        pi2.space_after = Pt(8)

    p_cmd = tf17_r.add_paragraph()
    p_cmd.text = "1-Command Zero-Config Run: mvn compile exec:java"
    p_cmd.font.name = "Arial"
    p_cmd.font.size = Pt(13)
    p_cmd.font.bold = True
    p_cmd.font.color.rgb = SUCCESS_COLOR

    prs.save(PPTX_PATH)
    print("Successfully generated PPTX at: " + PPTX_PATH)

def build_pdf():
    # Render PDF matching exact 16:9 slides using ReportLab Canvas
    width, height = 11.0 * 72, 6.1875 * 72  # 16:9 ratio in points (792 x 445.5)
    c = canvas.Canvas(PDF_PATH, pagesize=(width, height))

    # Helper to draw peach slide background
    def draw_bg(slide_title, category="ONLINE HEALTHCARE MANAGEMENT SYSTEM • REVIEW 1"):
        c.setFillColor(colors.HexColor("#FFFBF8"))
        c.rect(0, 0, width, height, fill=1, stroke=0)
        c.setFillColor(colors.HexColor("#FFFFFF"))
        c.setStrokeColor(colors.HexColor("#F3E3D8"))
        c.roundRect(20, 20, width - 40, height - 40, 8, fill=1, stroke=1)
        
        c.setFillColor(colors.HexColor("#C96F4A"))
        c.setFont("Helvetica-Bold", 8)
        c.drawString(40, height - 42, category.upper())
        
        c.setFillColor(colors.HexColor("#3A2B25"))
        c.setFont("Helvetica-Bold", 16)
        c.drawString(40, height - 64, slide_title)

    prs = Presentation(PPTX_PATH)
    total_slides = len(prs.slides)

    # We will draw each slide cleanly
    slides_info = [
        ("Title Slide", "Project Review 1 • Online Healthcare Management System"),
        ("Problem Statement & Objectives", "Clinical Challenge & Strategic Engineering Objectives"),
        ("User Roles & Key Features", "Multi-Stakeholder Feature Matrix (Admin, Doctor, Patient)"),
        ("Tech Stack & Architecture Rationale", "Enterprise Stack (Java 17, Servlet 6, JSP, JDBC, MySQL)"),
        ("System Architecture (MVC Layered)", "Layered MVC Architecture & Security Interceptor Pipeline"),
        ("Project Structure & Packages", "Clean Modular Project Structure & Layer Packaging"),
        ("System Use-Case Modeling", "Role-Based Access Control Actor Interactions"),
        ("Database Design: ER Diagram", "3NF Normalized Relational Schema Design"),
        ("Table Overview & Constraints", "12-Table Schema Specifications & Foreign Keys"),
        ("Database Connectivity (JDBC)", "HikariCP Connection Pool & /db-test Diagnostic Proof"),
        ("Core Java Concepts", "OOP, Generics, Exceptions, Multithreading & Transactions"),
        ("UI/UX Design System", "Warm Peach Design Tokens, Bento Grid & Typography"),
        ("Desktop Application Walkthrough", "Multi-Role Dashboards Desktop Experience"),
        ("Responsiveness & Mobile Adaptation", "Mobile (375px) vs Desktop (1280px) Responsiveness"),
        ("Servlets & Web Integration", "Lifecycle, Session Management, CSRF & Filters"),
        ("Milestones & Review 2 Plan", "Current Accomplishments & Review 2 Development Roadmap"),
        ("Repository & Demo Credentials", "Public GitHub Repository, Verification & Demo Access")
    ]

    for idx, (title, subtitle) in enumerate(slides_info):
        draw_bg(title, f"SLIDE {idx+1} OF {total_slides} • CAREFIRST HEALTHCARE OS")
        
        c.setFillColor(colors.HexColor("#8A7268"))
        c.setFont("Helvetica", 11)
        c.drawString(40, height - 82, subtitle)

        # Embed respective graphic if exists
        img_to_embed = None
        if idx == 0:
            c.setFillColor(colors.HexColor("#C96F4A"))
            c.setFont("Helvetica-Bold", 13)
            c.drawString(50, height - 130, "Online Healthcare Management System (CareFirst)")
            c.setFillColor(colors.HexColor("#3A2B25"))
            c.setFont("Helvetica", 11)
            c.drawString(50, height - 155, "Presenter: Deepanshu (Lead Developer & System Architect)")
            c.drawString(50, height - 175, "Course: Java Web-Based Application Development | Evaluation: Review 1")
            c.drawString(50, height - 195, "Stack: Jakarta Servlet 6.0 + JSP 3.1 + JDBC + HikariCP + MySQL 8.0")
            c.setFillColor(colors.HexColor("#7FA88B"))
            c.drawString(50, height - 225, "Rubric Mappings: Problem Understanding (8) • Core Java (10) • Database/JDBC (8) • Servlets (7)")
        elif idx == 4:
            img_to_embed = os.path.join(DOCS_DIR, "architecture-diagram.png")
        elif idx == 6:
            img_to_embed = os.path.join(DOCS_DIR, "use-case-diagram.png")
        elif idx == 7:
            img_to_embed = os.path.join(DOCS_DIR, "er-diagram.png")
        elif idx == 9:
            img_to_embed = os.path.join(SCREENSHOTS_DIR, "db-test-desktop.png")
        elif idx == 12:
            img_to_embed = os.path.join(SCREENSHOTS_DIR, "admin-dashboard-desktop.png")
        elif idx == 13:
            img_to_embed = os.path.join(SCREENSHOTS_DIR, "landing-desktop.png")
        elif idx == 16:
            img_to_embed = os.path.join(DOCS_DIR, "github-qr.png")

        if img_to_embed and os.path.exists(img_to_embed):
            try:
                if idx == 16:
                    c.drawImage(img_to_embed, 50, 60, width=160, height=160, preserveAspectRatio=True)
                    c.setFillColor(colors.HexColor("#3A2B25"))
                    c.setFont("Helvetica-Bold", 10)
                    c.drawString(240, 200, "Repository: https://github.com/ParvPrakash/healthcare-management-system-")
                    c.setFont("Helvetica", 9)
                    c.drawString(240, 180, "Admin: admin@carefirst.com / Admin@123")
                    c.drawString(240, 165, "Doctor: doctor.sarah@carefirst.com / Doctor@123")
                    c.drawString(240, 150, "Patient: john.doe@example.com / Patient@123")
                    c.setFillColor(colors.HexColor("#7FA88B"))
                    c.drawString(240, 125, "One-Command Local Run: mvn compile exec:java")
                else:
                    c.drawImage(img_to_embed, 40, 40, width=width - 80, height=height - 135, preserveAspectRatio=True)
            except Exception as e:
                print(f"Image draw error on slide {idx}: {e}")
        elif idx not in (0, 16):
            # Text content summary for text slides
            c.setFillColor(colors.HexColor("#3A2B25"))
            c.setFont("Helvetica", 10)
            if idx == 1:
                lines = [
                    "• The Problem: Fragmented clinic scheduling, double bookings, missing medical records, lack of RBAC.",
                    "• The Solution: 100% Java web architecture with Jakarta Servlets, normalized 3NF schema, ACID slot locks.",
                    "• Architecture: Strict MVC separation (model -> dao -> service -> servlet) + filters and listeners.",
                    "• Connectivity: Leased HikariCP connection pool with try-with-resources and 100% PreparedStatement security."
                ]
            elif idx == 2:
                lines = [
                    "• 👑 Administrator: User and doctor management, analytics KPIs, audit logs, system settings, DB diagnostics.",
                    "• 🩺 Doctor: Weekly recurring availability scheduler, patient appointment queue, EHR records & prescriptions.",
                    "• 👤 Patient: Specialist doctor catalog, live slot booking with concurrency lock, prescription and history review."
                ]
            elif idx == 3:
                lines = [
                    "• Java 17 LTS: Modern OOP, type safety, multithreading, and high performance.",
                    "• Jakarta Servlet 6.0: Fine-grained HTTP request lifecycle control and filter interceptor chains.",
                    "• Jakarta JSP 3.1 & JSTL 3.0: Dynamic server-side templating with automatic XSS output escaping.",
                    "• JDBC & HikariCP 5.1: Zero ORM overhead, connection pooling, PreparedStatement query parameters.",
                    "• MySQL 8.0+ / 9.x: Relational ACID storage engine with InnoDB foreign keys and B-Tree indexes."
                ]
            elif idx == 5:
                lines = [
                    "• model: Polymorphic user hierarchy (Doctor, Patient, Admin) and interfaces (Bookable, Reportable).",
                    "• dao: Generic Repository<T, ID> and functional RowMapper<T> for type-safe JDBC extraction.",
                    "• service: Business invariant validation, caching (DoctorCache), and multi-table transactions.",
                    "• servlet: HTTP presentation controllers inheriting lifecycle utilities from BaseServlet.",
                    "• filter: UTF-8 encoding, SLF4J request logging, tunnel security, auth check, and RBAC guard.",
                    "• listener: AppContextListener for startup/shutdown; AppSessionListener for active session tracking.",
                    "• util: HikariCP DBConnection, PasswordUtil (BCrypt), CsrfUtil, ApiResponse, OpenPDF generator."
                ]
            elif idx == 8:
                lines = [
                    "• users (16 rows) | doctors (5 rows) | patients (10 rows) | schedules (21 rows)",
                    "• time_slots (30 rows) | appointments (11 rows) | medical_records (5 rows) | feedback (5 rows)",
                    "• notifications (190 rows) | system_settings (11 rows) | audit_logs (8 rows) | login_attempts (0 rows)",
                    "• Constraints: PKs on all tables, 15 Foreign Keys with cascading actions, UNIQUE doctor slot locks, CHECK ratings."
                ]
            elif idx == 10:
                lines = [
                    "• OOP & Polymorphism: User base class specialized by Doctor, Patient, and Admin with contract interfaces.",
                    "• Generics & Collections: Generic Repository<T, ID>, RowMapper<T>, ApiResponse<T>, ConcurrentHashMap.",
                    "• Custom Exception Hierarchy: HealthcareException checked root with domain subclasses for auth, validation, and conflicts.",
                    "• Multithreading & Synchronization: SlotLockManager ReentrantLocks preventing concurrent double-booking clashing.",
                    "• Transaction Management: Explicit conn.setAutoCommit(false), commit(), and rollback() in multi-step booking operations."
                ]
            elif idx == 11:
                lines = [
                    "• Warm Peach Aesthetic: Custom CSS design tokens (--bg: #FFFBF8, --peach-500: #E8906B, --text: #3A2B25).",
                    "• Modern Typography: Fraunces serif headings paired with Plus Jakarta Sans body typography.",
                    "• Bento Grid Layout: Modular card spans with animated KPI counters and activity streams.",
                    "• Accessibility & Themes: High contrast compliance, micro-animations, and full dark mode support."
                ]
            elif idx == 14:
                lines = [
                    "• Servlet Lifecycle: Explicit overrides of init(), service(), doGet(), doPost(), and destroy() with SLF4J logging.",
                    "• Session Management: 30-minute HTTP sessions with session fixation defense (changeSessionId()).",
                    "• Cookie Hardening: HttpOnly, SameSite=Lax, and dynamic Secure attributes over HTTPS tunnels.",
                    "• Filter Pipeline: Chain of responsibility (EncodingFilter -> LoggingFilter -> TunnelFilter -> AuthFilter -> RoleFilter)."
                ]
            elif idx == 15:
                lines = [
                    "• Review 1 Status: 100% of architectural, database, connectivity, and UI/UX requirements completed and verified.",
                    "• Review 2 Focus: WebRTC telehealth video calls, automated email/SMS reminders, Chart.js analytics, OpenPDF reports."
                ]
            else:
                lines = []

            y_text = height - 120
            for line in lines:
                c.drawString(50, y_text, line)
                y_text -= 22

        c.showPage()

    c.save()
    print("Successfully generated PDF at: " + PDF_PATH)

if __name__ == "__main__":
    build_presentation()
    build_pdf()
