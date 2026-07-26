package vn.springboot.seed;

/**
 * One implementation per seeded domain table. {@link SeedRunner} orchestrates all
 * implementations in a fixed, FK-safe order — insert/delete order is correctness-critical
 * since {@code V1__init_db.sql} declares no real database-level FK constraints.
 */
public interface DomainSeeder {

    /** True if the domain's table currently has zero rows. */
    boolean isEmpty();

    /** Deletes every row this seeder owns (children-first if it owns a FK column). */
    void reset();

    /** Inserts the ported dataset. */
    void seed();
}
